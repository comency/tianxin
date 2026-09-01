<template>
  <div class="p-5">
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-lg font-medium">店铺运营统计</span>
          <div class="flex items-center gap-2">
            <el-input-number v-model="shopId" :min="1" :precision="0" controls-position="right" placeholder="店铺编号" />
            <el-date-picker v-model="times" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" />
            <el-button type="primary" :loading="loading" @click="loadData">查询</el-button>
          </div>
        </div>
      </template>
      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column prop="shopId" label="店铺编号" width="100" />
        <el-table-column prop="shopName" label="店铺名称" min-width="180" />
        <el-table-column prop="orderCount" label="订单数" width="100" />
        <el-table-column prop="paidOrderCount" label="已支付订单" width="120" />
        <el-table-column label="支付金额" width="130" align="right">
          <template #default="{ row }">￥{{ fen(row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column label="退款金额" width="130" align="right">
          <template #default="{ row }">￥{{ fen(row.refundAmount) }}</template>
        </el-table-column>
        <el-table-column label="已结算" width="130" align="right">
          <template #default="{ row }">￥{{ fen(row.settledAmount) }}</template>
        </el-table-column>
        <el-table-column label="待结算" width="130" align="right">
          <template #default="{ row }">￥{{ fen(row.unsettledAmount) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && rows.length === 0" description="暂无统计数据" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import * as ShopStatisticsApi from '@/api/mall/statistics/shop'

const loading = ref(false)
const shopId = ref<number>()
const times = ref<[string, string]>([
  dayjs().subtract(30, 'day').format('YYYY-MM-DD HH:mm:ss'),
  dayjs().format('YYYY-MM-DD HH:mm:ss')
])
const rows = ref<ShopStatisticsApi.ShopOperationStatisticsVO[]>([])

const fen = (value?: number) => ((value || 0) / 100).toFixed(2)

const loadData = async () => {
  loading.value = true
  try {
    rows.value = await ShopStatisticsApi.getShopOperationStatistics({ shopId: shopId.value, times: times.value })
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
