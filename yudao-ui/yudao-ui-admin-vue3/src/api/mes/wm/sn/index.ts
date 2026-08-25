import request from '@/config/axios'

// MES SN 码 VO
export interface WmSnVO {
  id: number
  uuid: string
  code: string
  itemId: number
  itemCode: string
  itemName: string
  specification: string
  batchCode: string
  workOrderId: number
  createTime: string
}

// MES SN 码生成 VO
export interface WmSnGenerateVO {
  itemId: number
  batchCode?: string
  workOrderId?: number
  count?: number
  snNum?: number
}

// MES SN 码分组 VO
export interface WmSnGroupVO {
  uuid: string
  count: number
  itemId: number
  itemCode: string
  itemName: string
  specification: string
  unitName: string
  batchCode: string
  workOrderId: number
  createTime: string
}

// MES SN 码 API
export const WmSnApi = {
  // 生成 SN 码
  generateSnCodes: async (data: WmSnGenerateVO) => {
    return await request.post({
      url: '/mes/wm/sn/generate',
      data: { ...data, count: data.count ?? data.snNum }
    })
  },

  // 查询 SN 码分组分页
  getSnPage: async (params: any) => {
    return await request.get({ url: '/mes/wm/sn/group-page', params })
  },

  // 查询 SN 码明细列表
  getSnListByUuid: async (uuid: string) => {
    return await request.get({ url: '/mes/wm/sn/list-by-uuid?uuid=' + uuid })
  },

  // 批量删除 SN 码
  deleteSnBatch: async (uuid: string) => {
    return await request.delete({ url: '/mes/wm/sn/delete-batch', params: { uuid } })
  },

  // 导出 SN 码 Excel
  exportSnExcel: async (params: any) => {
    return await request.download({ url: '/mes/wm/sn/export-excel', params })
  }
}

// 命名导出供新版 SN 码页面直接调用。
export const generateSnCodes = WmSnApi.generateSnCodes
export const getSnListByUuid = WmSnApi.getSnListByUuid
