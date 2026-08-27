import request from '@/sheep/request';

const ShopApi = {
  getShopList: () => request({ url: '/product/shop/list', method: 'GET' }),
  getShop: (id) => request({ url: '/product/shop/get', method: 'GET', params: { id } }),
};

export default ShopApi;
