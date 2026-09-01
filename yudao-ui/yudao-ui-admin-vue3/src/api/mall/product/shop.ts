import request from '@/config/axios'

/** 产业链企业店铺 */
export interface ShopVO {
  id?: number
  enterpriseId: number | undefined
  name: string
  logoUrl?: string
  contactName?: string
  contactMobile?: string
  managerUserId?: number
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

/** 获得当前后台账号负责的企业店铺；平台账号未绑定时返回空 */
export const getMyManagedShop = () => {
  return request.get({ url: '/product/shop/my' })
}
