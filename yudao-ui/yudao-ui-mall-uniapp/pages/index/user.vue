<!-- 个人中心：支持装修 -->
<template>
  <s-layout
    title="我的"
    tabbar="/pages/index/user"
    navbar="custom"
    :bgStyle="template.page"
    :navbarStyle="template.navigationBar"
    onShareAppMessage
  >
    <view class="member-brand"><view class="member-kicker">MEMBER SERVICE CENTER</view><view class="member-title">产业商城会员中心</view><view class="member-desc">订单、优惠与企业采购服务</view></view>
    <s-block
      v-for="(item, index) in template.components"
      :key="index"
      :styles="item.property.style"
    >
      <s-block-item :type="item.id" :data="item.property" :styles="item.property.style" />
    </s-block>
  </s-layout>
</template>

<script setup>
  import { computed } from 'vue';
  import { onShow, onPageScroll, onPullDownRefresh } from '@dcloudio/uni-app';
  import sheep from '@/sheep';

  // 隐藏原生tabBar
  uni.hideTabBar({
    fail: () => {},
  });

  const template = computed(() => sheep.$store('app').template.user);

  onShow(() => {
    sheep.$store('user').updateUserData();
  });

  onPullDownRefresh(() => {
    sheep.$store('user').updateUserData();
    setTimeout(function () {
      uni.stopPullDownRefresh();
    }, 800);
  });

  onPageScroll(() => {});
</script>

<style scoped lang="scss">
  .member-brand { margin: 20rpx; padding: 28rpx 30rpx; border-radius: 24rpx; background: linear-gradient(135deg, #143f5b, #28767b); color: #fff; box-shadow: 0 12rpx 26rpx rgba(20, 67, 89, .18); }
  .member-kicker { color: #a8d6d5; font-size: 18rpx; letter-spacing: 2rpx; }
  .member-title { margin-top: 8rpx; font-size: 36rpx; font-weight: 700; }
  .member-desc { margin-top: 10rpx; color: #d6ebeb; font-size: 22rpx; }
</style>
