<template>
  <s-layout title="企业店铺">
    <view v-if="shop" class="detail-page">
      <view class="shop-head"><image v-if="shop.logoUrl" class="logo" :src="shop.logoUrl" mode="aspectFill" /><view v-else class="logo logo-fallback">{{ shortName(shop.name) }}</view><view class="head-info"><view class="name">{{ shop.name }}</view><view class="tags"><text>认证企业</text><text>产业链商家</text></view></view></view>
      <view class="notice"><text class="notice-dot" />平台严选保温、防腐材料及工程服务企业</view>
      <view class="card"><view class="card-title">企业简介</view><view class="card-content">{{ shop.introduction || '专业防腐保温材料与工程服务供应商。' }}</view></view>
      <view class="card"><view class="card-title">联系企业</view><view class="contact-row"><text>联系人</text><text>{{ shop.contactName || '暂未填写' }}</text></view><view class="contact-row"><text>联系电话</text><text>{{ shop.contactMobile || '暂未填写' }}</text></view></view>
      <button class="goods-button" @tap="goGoods">查看店铺商品 <text>→</text></button>
    </view>
  </s-layout>
</template>
<script setup>
import { ref } from 'vue'; import { onLoad } from '@dcloudio/uni-app'; import sheep from '@/sheep'; import ShopApi from '@/sheep/api/product/shop';
const shop = ref(null); const shopId = ref(); const shortName = (name) => (name || '企').replace(/有限公司|股份有限公司|实业公司|集团/g, '').slice(0, 2);
onLoad(async (options) => { shopId.value = options.id; const { data } = await ShopApi.getShop(options.id); shop.value = data; });
const goGoods = () => sheep.$router.go('/pages/goods/list', { shopId: shopId.value });
</script>
<style scoped lang="scss">
.detail-page{min-height:100vh;padding:24rpx;background:#f4f7f8}.shop-head{display:flex;align-items:center;padding:32rpx;border-radius:24rpx 24rpx 0 0;background:linear-gradient(135deg,#123e5b,#23717b);color:#fff}.logo{width:104rpx;height:104rpx;border:3rpx solid rgba(255,255,255,.5);border-radius:20rpx;background:#fff}.logo-fallback{display:flex;align-items:center;justify-content:center;background:linear-gradient(135deg,#d7943b,#f0c273);color:#fff;font-size:31rpx;font-weight:700}.head-info{min-width:0;margin-left:22rpx}.name{overflow:hidden;font-size:33rpx;font-weight:700;text-overflow:ellipsis;white-space:nowrap}.tags{display:flex;gap:10rpx;margin-top:14rpx}.tags text{padding:4rpx 11rpx;border-radius:20rpx;background:rgba(255,255,255,.15);font-size:20rpx;color:#dceff1}.notice{display:flex;align-items:center;padding:18rpx 24rpx;border-radius:0 0 24rpx 24rpx;background:#e5f4f1;color:#39756d;font-size:22rpx}.notice-dot{width:10rpx;height:10rpx;margin-right:10rpx;border-radius:50%;background:#54a995}.card{margin-top:20rpx;padding:28rpx;border-radius:20rpx;background:#fff;box-shadow:0 7rpx 20rpx rgba(31,62,75,.05)}.card-title{position:relative;margin-bottom:18rpx;padding-left:16rpx;color:#193145;font-size:28rpx;font-weight:700}.card-title:before{position:absolute;left:0;top:5rpx;width:6rpx;height:27rpx;border-radius:6rpx;background:#e99833;content:''}.card-content{color:#60707a;font-size:25rpx;line-height:1.8}.contact-row{display:flex;justify-content:space-between;padding:16rpx 0;border-top:1rpx solid #eff2f4;color:#75828c;font-size:25rpx}.contact-row text:last-child{color:#273f50}.goods-button{margin-top:34rpx;border-radius:48rpx;background:linear-gradient(90deg,#1d6170,#2f8a91);color:#fff;font-size:29rpx;box-shadow:0 10rpx 20rpx rgba(28,104,111,.2)}.goods-button text{margin-left:12rpx;font-size:34rpx}
</style>
