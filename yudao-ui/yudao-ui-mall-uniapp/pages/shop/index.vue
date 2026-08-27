<template>
  <s-layout title="企业店铺">
    <view class="shop-page">
      <view class="hero">
        <view class="hero-glow hero-glow-one" /><view class="hero-glow hero-glow-two" />
        <view class="hero-content"><view class="eyebrow">INDUSTRIAL MATERIALS MARKET</view><view class="hero-title">保温防腐产业链</view><view class="hero-desc">精选上游原料、生产制造与工程服务企业</view><view class="hero-stats"><text>{{ shops.length }}</text><text>家认证企业店铺</text></view></view>
        <view class="hero-mark">工</view>
      </view>
      <view class="section-title"><view class="title-bar" /><text>推荐企业</text><text class="title-sub">品质供应 · 专业服务</text></view>
      <view class="shop-list">
        <view v-for="(shop, index) in shops" :key="shop.id" class="shop-card" @tap="openShop(shop.id)">
          <image v-if="shop.logoUrl" class="shop-logo" :src="shop.logoUrl" mode="aspectFill" />
          <view v-else class="shop-logo logo-fallback" :class="'logo-' + (index % 4)">{{ shortName(shop.name) }}</view>
          <view class="shop-body"><view class="shop-name-row"><text class="shop-name">{{ shop.name }}</text><text class="verified">认证</text></view><view class="shop-intro">{{ shop.introduction || '专业防腐保温材料与工程服务供应商' }}</view><view class="shop-footer"><text>进入店铺</text><text class="arrow">›</text></view></view>
        </view>
        <s-empty v-if="loaded && !shops.length" text="暂无店铺" />
      </view>
    </view>
  </s-layout>
</template>
<script setup>
import { ref } from 'vue';
import sheep from '@/sheep';
import ShopApi from '@/sheep/api/product/shop';
const shops = ref([]); const loaded = ref(false);
const shortName = (name) => (name || '企').replace(/有限公司|股份有限公司|实业公司|集团/g, '').slice(0, 2);
ShopApi.getShopList().then(({ data }) => { shops.value = data || []; }).finally(() => { loaded.value = true; });
const openShop = (shopId) => sheep.$router.go('/pages/shop/detail', { id: shopId });
</script>
<style scoped lang="scss">
.shop-page{min-height:100vh;padding:24rpx;background:linear-gradient(180deg,#edf5fb 0,#f6f8fa 420rpx,#f6f8fa 100%)}
.hero{position:relative;overflow:hidden;min-height:270rpx;border-radius:28rpx;background:linear-gradient(135deg,#103c5b,#0f6c78);box-shadow:0 16rpx 36rpx rgba(21,76,102,.22)}.hero-content{position:relative;z-index:2;padding:34rpx 32rpx;color:#fff}.eyebrow{font-size:18rpx;letter-spacing:2rpx;color:#a9d9dd}.hero-title{margin-top:12rpx;font-size:42rpx;font-weight:700;letter-spacing:2rpx}.hero-desc{margin-top:12rpx;font-size:23rpx;color:#d8edf0}.hero-stats{display:inline-flex;gap:10rpx;align-items:baseline;margin-top:22rpx;padding:9rpx 16rpx;border:1rpx solid rgba(255,255,255,.2);border-radius:30rpx;background:rgba(255,255,255,.1);font-size:21rpx}.hero-stats text:first-child{font-size:30rpx;font-weight:700;color:#f8cb7a}.hero-mark{position:absolute;right:34rpx;bottom:-38rpx;color:rgba(255,255,255,.1);font-size:200rpx;font-weight:700}.hero-glow{position:absolute;border-radius:50%;background:rgba(105,219,207,.16)}.hero-glow-one{width:220rpx;height:220rpx;right:-80rpx;top:-90rpx}.hero-glow-two{width:130rpx;height:130rpx;right:145rpx;bottom:-80rpx}
.section-title{display:flex;align-items:center;margin:32rpx 8rpx 18rpx;color:#17324a;font-size:32rpx;font-weight:700}.title-bar{width:7rpx;height:30rpx;margin-right:14rpx;border-radius:8rpx;background:#e99631}.title-sub{margin-left:auto;color:#8a9aaa;font-size:21rpx;font-weight:400}.shop-list{padding-bottom:36rpx}.shop-card{display:flex;align-items:center;padding:24rpx;margin-bottom:18rpx;border:1rpx solid #edf0f2;border-radius:22rpx;background:#fff;box-shadow:0 8rpx 20rpx rgba(32,65,86,.055)}.shop-logo{flex:none;width:92rpx;height:92rpx;border-radius:18rpx}.logo-fallback{display:flex;align-items:center;justify-content:center;color:#fff;font-size:28rpx;font-weight:700}.logo-0{background:linear-gradient(135deg,#1c7180,#56b8ae)}.logo-1{background:linear-gradient(135deg,#4a5e91,#809be0)}.logo-2{background:linear-gradient(135deg,#aa6a36,#e5ad65)}.logo-3{background:linear-gradient(135deg,#627253,#9cae76)}.shop-body{flex:1;min-width:0;margin-left:20rpx}.shop-name-row{display:flex;align-items:center}.shop-name{overflow:hidden;text-overflow:ellipsis;white-space:nowrap;color:#1b3143;font-size:29rpx;font-weight:650}.verified{flex:none;margin-left:10rpx;padding:3rpx 9rpx;border-radius:7rpx;background:#edf7f4;color:#318a72;font-size:18rpx}.shop-intro{overflow:hidden;margin-top:10rpx;color:#7b8994;font-size:22rpx;line-height:1.4;text-overflow:ellipsis;white-space:nowrap}.shop-footer{display:flex;align-items:center;gap:7rpx;margin-top:10rpx;color:#387d92;font-size:21rpx}.arrow{font-size:32rpx;line-height:20rpx}
</style>
