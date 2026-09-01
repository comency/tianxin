import request from '@/config/axios'

export interface ShopOperationStatisticsVO {
  shopId: number
  shopName: string
  orderCount: number
  paidOrderCount: number
  paidAmount: number
  refundOrderCount: number
  refundAmount: number
  settledAmount: number
  unsettledAmount: number
}

export const getShopOperationStatistics = (params?: {
  shopId?: number
  times?: string[]
}) => {
  return request.get<ShopOperationStatisticsVO[]>({
    url: '/statistics/shop/operation',
    params
  })
}
