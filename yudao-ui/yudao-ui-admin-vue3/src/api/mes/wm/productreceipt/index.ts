import request from '@/config/axios'

// MES 产品收货单 VO
export interface WmProductReceiptVO {
  id: number
  code: string
  name: string
  workOrderId: number
  workOrderCode: string
  itemId: number
  itemCode: string
  itemName: string
  specification: string
  unitMeasureName: string
  receiptDate: string
  status: number
  remark: string
  createTime: string
}

// MES 产品收货单 API
export const WmProductReceiptApi = {
  // 查询产品收货单分页
  getProductReceiptPage: async (params: any) => {
    return await request.get({ url: '/mes/wm/product-receipt/page', params })
  },

  // 查询产品收货单详情
  getProductReceipt: async (id: number) => {
    return await request.get({ url: '/mes/wm/product-receipt/get?id=' + id })
  },

  // 新增产品收货单
  createProductReceipt: async (data: WmProductReceiptVO) => {
    return await request.post({ url: '/mes/wm/product-receipt/create', data })
  },

  // 修改产品收货单
  updateProductReceipt: async (data: WmProductReceiptVO) => {
    return await request.put({ url: '/mes/wm/product-receipt/update', data })
  },

  // 删除产品收货单
  deleteProductReceipt: async (id: number) => {
    return await request.delete({ url: '/mes/wm/product-receipt/delete?id=' + id })
  },

  // 提交产品收货单
  submitProductReceipt: async (id: number) => {
    return await request.put({ url: '/mes/wm/product-receipt/submit?id=' + id })
  },

  // 执行上架
  checkProductReceiptQuantity: async (id: number) => {
    return await request.get({ url: '/mes/wm/product-receipt/check-quantity?id=' + id })
  },

  // 执行入库
  stockProductReceipt: async (id: number) => {
    return await request.put({ url: '/mes/wm/product-receipt/stock?id=' + id })
  },

  // 取消产品收货单
  finishProductReceipt: async (id: number) => {
    return await request.put({ url: '/mes/wm/product-receipt/finish?id=' + id })
  },

  // 导出产品收货单 Excel
  cancelProductReceipt: async (id: number) => {
    return await request.put({ url: '/mes/wm/product-receipt/cancel?id=' + id })
  },

  // 导出产品收货单 Excel
  exportProductReceipt: async (params: any) => {
    return await request.download({ url: '/mes/wm/product-receipt/export-excel', params })
  }
}
