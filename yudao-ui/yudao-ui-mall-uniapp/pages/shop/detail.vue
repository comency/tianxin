<template>
  <s-layout title="企业店铺">
    <view v-if="shop" class="detail-page">
      <view class="shop-head"
        ><image v-if="shop.logoUrl" class="logo" :src="shop.logoUrl" mode="aspectFill" /><view
          v-else
          class="logo logo-fallback"
          >{{ shortName(shop.name) }}</view
        ><view class="head-info"
          ><view class="name">{{ shop.name }}</view
          ><view class="tags"><text>认证企业</text><text>产业链商家</text></view></view
        ></view
      >
      <view class="notice"><text class="notice-dot" />平台严选保温、防腐材料及工程服务企业</view>
      <view class="card"
        ><view class="card-title">企业简介</view
        ><view class="card-content">{{
          shop.introduction || '专业防腐保温材料与工程服务供应商。'
        }}</view></view
      >
      <view class="card"
        ><view class="card-title">联系企业</view
        ><view class="contact-row"
          ><text>联系人</text><text>{{ shop.contactName || '暂未填写' }}</text></view
        ><view class="contact-row"
          ><text>联系电话</text><text>{{ shop.contactMobile || '暂未填写' }}</text></view
        ></view
      >
      <view v-if="productsLoaded" class="card goods-preview-card">
        <view class="card-title goods-title-row"
          ><text>店铺商品</text><text class="goods-count">{{ productTotal }} 件在售</text></view
        >
        <view v-if="products.length" class="goods-preview-list">
          <view
            v-for="product in products"
            :key="product.id"
            class="goods-preview"
            @tap="goProduct(product.id)"
          >
            <image
              v-if="product.picUrl"
              class="goods-pic"
              :src="product.picUrl"
              mode="aspectFill"
            />
            <view v-else class="goods-pic goods-pic-fallback">材</view>
            <view class="goods-info">
              <view class="goods-name">{{ product.name }}</view>
              <view class="goods-price">¥{{ fen2yuan(product.price) }}</view>
            </view>
            <text class="goods-arrow">›</text>
          </view>
        </view>
        <view v-else class="empty-goods">该企业暂未上架商品，可先电话咨询</view>
      </view>
      <view class="action-group">
        <button class="contact-button" @tap="contactShop">立即联系企业</button>
        <button class="goods-button" @tap="goGoods">查看店铺商品 <text>→</text></button>
      </view>
    </view>
  </s-layout>
</template>
<script setup>
  import { ref } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import ShopApi from '@/sheep/api/product/shop';
  import SpuApi from '@/sheep/api/product/spu';
  import { fen2yuan } from '@/sheep/hooks/useGoods';
  const shop = ref(null);
  const shopId = ref();
  const products = ref([]);
  const productTotal = ref(0);
  const productsLoaded = ref(false);
  const shortName = (name) =>
    (name || '企').replace(/有限公司|股份有限公司|实业公司|集团/g, '').slice(0, 2);
  onLoad(async (options) => {
    shopId.value = options.id;
    const { data } = await ShopApi.getShop(options.id);
    shop.value = data;
    try {
      const productResult = await SpuApi.getSpuPage({ pageNo: 1, pageSize: 3, shopId: options.id });
      if (productResult.code === 0) {
        products.value = productResult.data.list || [];
        productTotal.value = productResult.data.total || 0;
      }
    } finally {
      productsLoaded.value = true;
    }
  });
  const goGoods = () => sheep.$router.go('/pages/goods/list', { shopId: shopId.value });
  const goProduct = (id) => sheep.$router.go('/pages/goods/index', { id });
  const contactShop = () => {
    const mobile = shop.value?.contactMobile;
    if (!mobile) {
      sheep.$helper.toast('该企业暂未填写联系电话');
      return;
    }
    uni.makePhoneCall({
      phoneNumber: String(mobile),
      fail: () => uni.setClipboardData({ data: String(mobile), showToast: false }),
    });
  };
</script>
<style scoped lang="scss">
  .detail-page {
    min-height: 100vh;
    padding: 24rpx;
    background: #f4f7f8;
  }
  .shop-head {
    display: flex;
    align-items: center;
    padding: 32rpx;
    border-radius: 24rpx 24rpx 0 0;
    background: linear-gradient(135deg, #123e5b, #23717b);
    color: #fff;
  }
  .logo {
    width: 104rpx;
    height: 104rpx;
    border: 3rpx solid rgba(255, 255, 255, 0.5);
    border-radius: 20rpx;
    background: #fff;
  }
  .logo-fallback {
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #d7943b, #f0c273);
    color: #fff;
    font-size: 31rpx;
    font-weight: 700;
  }
  .head-info {
    min-width: 0;
    margin-left: 22rpx;
  }
  .name {
    overflow: hidden;
    font-size: 33rpx;
    font-weight: 700;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .tags {
    display: flex;
    gap: 10rpx;
    margin-top: 14rpx;
  }
  .tags text {
    padding: 4rpx 11rpx;
    border-radius: 20rpx;
    background: rgba(255, 255, 255, 0.15);
    font-size: 20rpx;
    color: #dceff1;
  }
  .notice {
    display: flex;
    align-items: center;
    padding: 18rpx 24rpx;
    border-radius: 0 0 24rpx 24rpx;
    background: #e5f4f1;
    color: #39756d;
    font-size: 22rpx;
  }
  .notice-dot {
    width: 10rpx;
    height: 10rpx;
    margin-right: 10rpx;
    border-radius: 50%;
    background: #54a995;
  }
  .card {
    margin-top: 20rpx;
    padding: 28rpx;
    border-radius: 20rpx;
    background: #fff;
    box-shadow: 0 7rpx 20rpx rgba(31, 62, 75, 0.05);
  }
  .card-title {
    position: relative;
    margin-bottom: 18rpx;
    padding-left: 16rpx;
    color: #193145;
    font-size: 28rpx;
    font-weight: 700;
  }
  .card-title:before {
    position: absolute;
    left: 0;
    top: 5rpx;
    width: 6rpx;
    height: 27rpx;
    border-radius: 6rpx;
    background: #e99833;
    content: '';
  }
  .card-content {
    color: #60707a;
    font-size: 25rpx;
    line-height: 1.8;
  }
  .contact-row {
    display: flex;
    justify-content: space-between;
    padding: 16rpx 0;
    border-top: 1rpx solid #eff2f4;
    color: #75828c;
    font-size: 25rpx;
  }
  .contact-row text:last-child {
    color: #273f50;
  }
  .goods-preview-card {
    padding-bottom: 16rpx;
  }
  .goods-title-row {
    display: flex;
    justify-content: space-between;
  }
  .goods-count {
    color: #8b9aa3;
    font-size: 21rpx;
    font-weight: 400;
  }
  .goods-preview-list {
    display: flex;
    flex-direction: column;
    gap: 18rpx;
  }
  .goods-preview {
    display: flex;
    align-items: center;
    padding: 12rpx 0;
  }
  .goods-pic {
    flex: none;
    width: 98rpx;
    height: 98rpx;
    border-radius: 14rpx;
    background: #eef3f4;
  }
  .goods-pic-fallback {
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #5a7b83, #9fb8b8);
    color: #fff;
    font-size: 30rpx;
    font-weight: 700;
  }
  .goods-info {
    flex: 1;
    min-width: 0;
    margin-left: 18rpx;
  }
  .goods-name {
    overflow: hidden;
    color: #314752;
    font-size: 25rpx;
    line-height: 1.45;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .goods-price {
    margin-top: 10rpx;
    color: #db7d28;
    font-size: 25rpx;
    font-weight: 700;
  }
  .goods-arrow {
    margin-left: 12rpx;
    color: #96a3aa;
    font-size: 38rpx;
  }
  .empty-goods {
    padding: 12rpx 0 8rpx;
    color: #8b9aa3;
    font-size: 23rpx;
  }
  .action-group {
    display: flex;
    gap: 18rpx;
    margin-top: 34rpx;
  }
  .action-group button {
    flex: 1;
    margin: 0;
    border-radius: 48rpx;
    font-size: 27rpx;
  }
  .contact-button {
    border: 1rpx solid #2f8a91;
    background: #fff;
    color: #236f77;
  }
  .goods-button {
    background: linear-gradient(90deg, #1d6170, #2f8a91);
    color: #fff;
    box-shadow: 0 10rpx 20rpx rgba(28, 104, 111, 0.2);
  }
  .goods-button text {
    margin-left: 12rpx;
    font-size: 34rpx;
  }
</style>
