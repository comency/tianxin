import request from '@/config/axios'

export interface ShopSettlementOrderVO {
  id: number
  orderId: number
  orderNo: string
  orderFinishTime: string
  payAmount: number
  refundAmount: number
  settlementBaseAmount: number
  platformCommissionAmount: number
  settlementAmount: number
}

export interface ShopSettlementVO {
  id: number
  no: string
  shopId: number
  shopName: string
  periodStartTime: string
  periodEndTime: string
  orderCount: number
  orderPayAmount: number
  refundAmount: number
  settlementBaseAmount: number
  commissionRate: number
  platformCommissionAmount: number
  settlementAmount: number
  status: number
  auditUserId?: number
  auditTime?: string
  auditRemark?: string
  settleUserId?: number
  settleTime?: string
  createTime: string
  orders?: ShopSettlementOrderVO[]
}

export interface ShopSettlementGenerateReqVO {
  shopId: number | undefined
  periodStartTime: number
  periodEndTime: number
  commissionRate: number
}

export const getShopSettlementPage = (params: any) =>
  request.get<PageResult<ShopSettlementVO[]>>({ url: '/trade/shop-settlement/page', params })

export const getShopSettlement = (id: number) =>
  request.get<ShopSettlementVO>({ url: `/trade/shop-settlement/get?id=${id}` })

export const generateShopSettlement = (data: ShopSettlementGenerateReqVO) =>
  request.post<number>({ url: '/trade/shop-settlement/generate', data })

export const auditShopSettlement = (data: {
  id: number
  approved: boolean
  auditRemark?: string
}) => request.put({ url: '/trade/shop-settlement/audit', data })

export const confirmShopSettlement = (id: number) =>
  request.put({ url: `/trade/shop-settlement/confirm?id=${id}` })
