<script setup>
  import { onLaunch, onShow, onError } from '@dcloudio/uni-app';
  import { ShoproInit } from './sheep';

  onLaunch(() => {
    // 隐藏原生导航栏 使用自定义底部导航
    uni.hideTabBar({
      fail: () => {},
    });

    // 加载Shopro底层依赖
    ShoproInit();
  });

  onShow(() => {
    // #ifdef APP-PLUS
    // 获取urlSchemes参数
    const args = plus.runtime.arguments;
    if (args) {
    }

    // 获取剪贴板
    uni.getClipboardData({
      success: (res) => {},
    });
    // #endif
  });
</script>

<style lang="scss">
  @import '@/sheep/scss/index.scss';

  /* 防腐保温产业商城：为既有的商品、订单、营销与会员页面提供统一视觉基线 */
  page { background: #f4f7f8; color: #223947; }
  .bg-white { border-radius: 18rpx; box-shadow: 0 7rpx 18rpx rgba(32, 68, 82, .05); }
  .uni-load-more { color: #81909a; }
  button.ui-BG-Main-Gradient { background: linear-gradient(90deg, #1d6570, #2f9294) !important; }
</style>
