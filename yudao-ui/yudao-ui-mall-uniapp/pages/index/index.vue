<!-- 首页，支持店铺装修 -->
<template>
  <view v-if="template">
    <s-layout
      title="首页"
      navbar="custom"
      tabbar="/pages/index/index"
      :bgStyle="template.page"
      :navbarStyle="template.navigationBar"
      onShareAppMessage
    >
      <view class="industry-portal">
        <view class="portal-glow portal-glow-one" /><view class="portal-glow portal-glow-two" />
        <view class="portal-content"><view class="portal-kicker">INDUSTRIAL INSULATION MARKET</view><view class="portal-title">防腐保温产业商城</view><view class="portal-desc">材料采购 · 企业直供 · 工程服务</view><view class="portal-action" @tap="goShops">进入产业链企业店铺 <text>›</text></view></view>
        <view class="portal-symbol">保</view>
      </view>
      <view class="quick-panel">
        <view v-for="item in quickEntries" :key="item.name" class="quick-item" @tap="goQuick(item.path)"><view class="quick-icon" :class="item.color">{{ item.icon }}</view><text>{{ item.name }}</text></view>
      </view>
      <view v-if="shops.length" class="industry-shops">
        <view class="section-head"><view><text class="section-title">产业链企业店铺</text><text class="section-tip">严选认证</text></view><text class="more" @tap="goShops">查看全部 ›</text></view>
        <scroll-view scroll-x class="shop-scroll"><view v-for="(shop, index) in shops" :key="shop.id" class="shop-card" @tap="goShop(shop.id)"><image v-if="shop.logoUrl" :src="shop.logoUrl" class="shop-logo" mode="aspectFill" /><view v-else class="shop-logo logo-fallback" :class="'logo-' + (index % 4)">{{ shortName(shop.name) }}</view><text class="shop-name">{{ shop.name }}</text><text class="shop-intro">{{ shop.introduction || '专业产业链企业' }}</text></view></scroll-view>
      </view>
      <s-block
        v-for="(item, index) in template.components"
        :key="index"
        :styles="item.property.style"
      >
        <s-block-item :type="item.id" :data="item.property" :styles="item.property.style" />
      </s-block>
    </s-layout>
    <view class="industry-float" @tap="goShops">企业店铺</view>
  </view>
</template>

<script setup>
  import { computed, ref, onMounted } from 'vue';
  import { onLoad, onShow, onPageScroll, onPullDownRefresh } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import $share from '@/sheep/platform/share';
  import ShopApi from '@/sheep/api/product/shop';
  // 隐藏原生tabBar
  uni.hideTabBar({
    fail: () => {},
  });

  const template = computed(() => sheep.$store('app').template?.home);
  const shops = ref([]);
  const quickEntries = [
    { name: '保温材料', icon: '保', color: 'teal', path: '/pages/goods/list' },
    { name: '防腐涂料', icon: '防', color: 'blue', path: '/pages/goods/list' },
    { name: '工程服务', icon: '工', color: 'orange', path: '/pages/shop/index' },
    { name: '企业店铺', icon: '企', color: 'green', path: '/pages/shop/index' },
  ];
  onMounted(async () => { const { data } = await ShopApi.getShopList(); shops.value = data?.slice(0, 6) || []; });
  const goShops = () => sheep.$router.go('/pages/shop/index');
  const goShop = (id) => sheep.$router.go('/pages/shop/detail', { id });
  const goQuick = (path) => sheep.$router.go(path);
  const shortName = (name) => (name || '企').replace(/有限公司|股份有限公司|实业公司|集团/g, '').slice(0, 2);
  // 在此处拦截改变一下首页轮播图 此处先写死后期复活 放到启动函数里
  // (async function() {
  // console.log('原代码首页定制化数据',template)
  // let {
  // 	data
  // } = await index2Api.decorate();
  // console.log('首页导航配置化过高无法兼容',JSON.parse(data[1].value))
  // 改变首页底部数据 但是没有通过数组id获取商品数据接口
  // let {
  // 	data: datas
  // } = await index2Api.spids();
  // template.value.data[9].data.goodsIds = datas.list.map(item => item.id);
  // template.value.data[0].data.list = JSON.parse(data[0].value).map(item => {
  // 	return {
  // 		src: item.picUrl,
  // 		url: item.url,
  // 		title: item.name,
  // 		type: "image"
  // 	}
  // })
  // }())

  onLoad((options) => {
    // #ifdef MP
    // 小程序识别二维码
    if (options.scene) {
      const sceneParams = decodeURIComponent(options.scene).split('=');
      console.log('sceneParams=>', sceneParams);
      options[sceneParams[0]] = sceneParams[1];
    }
    // #endif

    // 预览模板
    if (options.templateId) {
      sheep.$store('app').init(options.templateId);
    }

    // 解析分享信息
    if (options.spm) {
      $share.decryptSpm(options.spm);
    }

    // 进入指定页面(完整页面路径)
    if (options.page) {
      sheep.$router.go(decodeURIComponent(options.page));
    }
  });

  onShow(async() => {
    // #ifdef APP-PLUS
    // ios首次授权网络，需要重新加载一次应用初始化
    // 可能需要考虑上uni.onNetworkStatusChange，uni.offNetworkStatusChange组合拳以及主动主动唤起权限申请
    // 一开始放app.vue，感觉负载太重，搬到这里来了。
    // 如果你的首页不是这个页面，需要把代码搬过去。
    if (sheep.$platform.os === 'ios') {
      if (await sheep.$platform.checkNetwork()) {
        await sheep.$store('app').init();
      }
    }
    // #endif
  });

  // 下拉刷新
  onPullDownRefresh(() => {
    sheep.$store('app').init();
    setTimeout(function () {
      uni.stopPullDownRefresh();
    }, 800);
  });

  onPageScroll(() => {});
</script>

<style scoped lang="scss">
.industry-portal{position:relative;overflow:hidden;min-height:278rpx;margin:20rpx 20rpx 0;border-radius:28rpx;background:linear-gradient(135deg,#123f5d,#0e7076);box-shadow:0 16rpx 35rpx rgba(16,75,96,.22)}.portal-content{position:relative;z-index:2;padding:32rpx;color:#fff}.portal-kicker{font-size:18rpx;letter-spacing:2rpx;color:#a8d9dc}.portal-title{margin-top:10rpx;font-size:42rpx;font-weight:700;letter-spacing:2rpx}.portal-desc{margin-top:13rpx;color:#d7eced;font-size:23rpx}.portal-action{display:inline-flex;align-items:center;gap:12rpx;margin-top:23rpx;padding:10rpx 18rpx;border-radius:30rpx;background:#e9a142;color:#fff;font-size:22rpx;box-shadow:0 6rpx 15rpx rgba(0,0,0,.14)}.portal-action text{font-size:31rpx;line-height:20rpx}.portal-symbol{position:absolute;right:34rpx;bottom:-45rpx;color:rgba(255,255,255,.1);font-size:205rpx;font-weight:700}.portal-glow{position:absolute;border-radius:50%;background:rgba(96,221,207,.16)}.portal-glow-one{width:220rpx;height:220rpx;right:-55rpx;top:-105rpx}.portal-glow-two{width:120rpx;height:120rpx;right:145rpx;bottom:-75rpx}.quick-panel{display:flex;justify-content:space-around;margin:0 20rpx;padding:24rpx 10rpx 20rpx;border-radius:0 0 24rpx 24rpx;background:#fff;box-shadow:0 10rpx 24rpx rgba(36,72,89,.07)}.quick-item{display:flex;flex-direction:column;align-items:center;gap:10rpx;color:#415260;font-size:22rpx}.quick-icon{display:flex;align-items:center;justify-content:center;width:64rpx;height:64rpx;border-radius:18rpx;color:#fff;font-size:28rpx;font-weight:700}.teal{background:linear-gradient(135deg,#298d8d,#68c2b9)}.blue{background:linear-gradient(135deg,#3e75aa,#7ca9d7)}.orange{background:linear-gradient(135deg,#c77a35,#edb464)}.green{background:linear-gradient(135deg,#5f8160,#9dbd7e)}.industry-float{position:fixed;right:24rpx;bottom:180rpx;z-index:99;padding:18rpx 23rpx;border:2rpx solid rgba(255,255,255,.55);background:linear-gradient(135deg,#1b6875,#32959b);color:#fff;border-radius:40rpx;font-size:26rpx;box-shadow:0 6rpx 18rpx rgba(22,100,111,.3)}.industry-shops{margin:20rpx;padding:25rpx 24rpx;background:#fff;border-radius:22rpx;box-shadow:0 8rpx 20rpx rgba(32,65,86,.05)}.section-head{display:flex;justify-content:space-between;align-items:center;margin-bottom:20rpx}.section-title{color:#193247;font-size:32rpx;font-weight:700}.section-tip{margin-left:12rpx;padding:4rpx 10rpx;border-radius:12rpx;background:#eaf6f2;color:#438c74;font-size:18rpx}.more{font-size:23rpx;color:#5d8994}.shop-scroll{white-space:nowrap}.shop-card{display:inline-flex;vertical-align:top;flex-direction:column;width:205rpx;margin-right:20rpx;white-space:normal}.shop-logo{width:78rpx;height:78rpx;border-radius:16rpx}.logo-fallback{display:flex;align-items:center;justify-content:center;color:#fff;font-size:26rpx;font-weight:700}.logo-0{background:linear-gradient(135deg,#1c7180,#56b8ae)}.logo-1{background:linear-gradient(135deg,#4a5e91,#809be0)}.logo-2{background:linear-gradient(135deg,#aa6a36,#e5ad65)}.logo-3{background:linear-gradient(135deg,#627253,#9cae76)}.shop-name{overflow:hidden;margin-top:12rpx;color:#203647;font-size:26rpx;font-weight:650;text-overflow:ellipsis;white-space:nowrap}.shop-intro{display:-webkit-box;overflow:hidden;margin-top:6rpx;color:#87939b;font-size:21rpx;-webkit-box-orient:vertical;-webkit-line-clamp:2}
</style>
