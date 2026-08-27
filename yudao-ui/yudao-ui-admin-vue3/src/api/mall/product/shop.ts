import request from '@/config/axios'

/** 产业链企业店铺 */
export interface ShopVO {
  id?: number
  enterpriseId: number | undefined
  name: string
  logoUrl?: string
  contactName?: string
  contactMobile?: string
  introduction?: string
  status: number
  createTime?: Date
}

export const createShop = (data: ShopVO) => {
  return request.post({ url: '/product/shop/create', data })
}

export const updateShop = (data: ShopVO) => {
  return request.put({ url: '/product/shop/update', data })
}

export const deleteShop = (id: number) => {
  return request.delete({ url: `/product/shop/delete?id=${id}` })
}

export const getShop = (id: number) => {
  return request.get({ url: `/product/shop/get?id=${id}` })
}

export const getShopList = () => {
  return request.get({ url: '/product/shop/list' })
}
