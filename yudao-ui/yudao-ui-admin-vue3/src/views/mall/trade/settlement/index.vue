<template>
  <el-alert
    class="mb-16px"
    :closable="false"
    show-icon
    title="结算金额 = 已完成订单实付金额 - 退款金额 - 平台佣金；同一订单不会重复进入有效结算单。"
    type="info"
  />

  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" label-width="82px">
      <el-form-item label="结算单号" prop="no">
        <el-input
          v-model="queryParams.no"
          class="!w-240px"
          clearable
          placeholder="请输入结算单号"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属店铺" prop="shopId">
        <el-select v-model="queryParams.shopId" class="!w-240px" clearable placeholder="全部店铺">
          <el-option v-for="shop in shopList" :key="shop.id" :label="shop.name" :value="shop.id!" />
        </el-select>
      </el-form-item>
      <el-form-item label="结算状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-200px" clearable placeholder="全部状态">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-340px"
          end-placeholder="结束时间"
          start-placeholder="开始时间"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon class="mr-5px" icon="ep:search" />搜索</el-button>
        <el-button @click="resetQuery"><Icon class="mr-5px" icon="ep:refresh" />重置</el-button>
        <el-button
          v-hasPermi="['trade:shop-settlement:create']"
          type="primary"
          @click="openGenerateDialog"
        >
          <Icon class="mr-5px" icon="ep:plus" />生成结算单
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-row :gutter="16" class="mb-16px">
      <el-col :span="6"
        ><el-statistic title="当前页订单实付" :value="pageSummary.pay" :precision="2" prefix="¥"
      /></el-col>
      <el-col :span="6"
        ><el-statistic title="当前页退款" :value="pageSummary.refund" :precision="2" prefix="¥"
      /></el-col>
      <el-col :span="6"
        ><el-statistic
          title="当前页平台佣金"
          :value="pageSummary.commission"
          :precision="2"
          prefix="¥"
      /></el-col>
      <el-col :span="6"
        ><el-statistic
          title="当前页店铺应结"
          :value="pageSummary.settlement"
          :precision="2"
          prefix="¥"
      /></el-col>
    </el-row>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column align="center" label="结算单号" min-width="190" prop="no" />
      <el-table-column align="center" label="店铺" min-width="150" prop="shopName" />
      <el-table-column align="center" label="结算周期" min-width="285">
        <template #default="{ row }">
          {{ formatDate(row.periodStartTime) }} 至 {{ formatDate(row.periodEndTime) }}
        </template>
      </el-table-column>
      <el-table-column align="center" label="订单数" width="85" prop="orderCount" />
      <el-table-column align="right" label="实付金额" width="110">
        <template #default="{ row }">¥{{ fenToYuan(row.orderPayAmount) }}</template>
      </el-table-column>
      <el-table-column align="right" label="退款" width="100">
        <template #default="{ row }">¥{{ fenToYuan(row.refundAmount) }}</template>
      </el-table-column>
      <el-table-column align="center" label="佣金比例" width="95">
        <template #default="{ row }">{{ (row.commissionRate / 100).toFixed(2) }}%</template>
      </el-table-column>
      <el-table-column align="right" label="平台佣金" width="110">
        <template #default="{ row }">¥{{ fenToYuan(row.platformCommissionAmount) }}</template>
      </el-table-column>
      <el-table-column align="right" label="店铺应结" width="115">
        <template #default="{ row }"
          ><b class="text-primary">¥{{ fenToYuan(row.settlementAmount) }}</b></template
        >
      </el-table-column>
      <el-table-column align="center" label="状态" width="95">
        <template #default="{ row }">
          <el-tag :type="statusMeta(row.status).type">{{ statusMeta(row.status).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" fixed="right" label="操作" width="230">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
          <template v-if="row.status === 0">
            <el-button
              v-hasPermi="['trade:shop-settlement:audit']"
              link
              type="success"
              @click="handleAudit(row, true)"
              >通过</el-button
            >
            <el-button
              v-hasPermi="['trade:shop-settlement:audit']"
              link
              type="danger"
              @click="handleAudit(row, false)"
              >驳回</el-button
            >
          </template>
          <el-button
            v-if="row.status === 10"
            v-hasPermi="['trade:shop-settlement:confirm']"
            link
            type="warning"
            @click="handleConfirm(row)"
            >确认结算</el-button
          >
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <el-dialog v-model="generateVisible" title="生成店铺结算单" width="560px">
    <el-form ref="generateFormRef" :model="generateForm" :rules="generateRules" label-width="110px">
      <el-form-item label="结算店铺" prop="shopId">
        <el-select v-model="generateForm.shopId" class="w-full" filterable placeholder="请选择店铺">
          <el-option v-for="shop in shopList" :key="shop.id" :label="shop.name" :value="shop.id!" />
        </el-select>
      </el-form-item>
      <el-form-item label="结算周期" prop="period">
        <el-date-picker
          v-model="generateForm.period"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="w-full"
          end-placeholder="结束时间"
          start-placeholder="开始时间"
          type="datetimerange"
          value-format="x"
        />
      </el-form-item>
      <el-form-item label="平台佣金" prop="commissionPercent">
        <el-input-number
          v-model="generateForm.commissionPercent"
          :max="100"
          :min="0"
          :precision="2"
          :step="0.5"
        />
        <span class="ml-8px">%</span>
      </el-form-item>
      <el-alert
        :closable="false"
        title="仅纳入周期内已支付且已完成、并且尚未结算的订单。"
        type="warning"
      />
    </el-form>
    <template #footer>
      <el-button @click="generateVisible = false">取消</el-button>
      <el-button :loading="generateLoading" type="primary" @click="submitGenerate">生成</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="detailVisible" title="结算单详情" width="980px">
    <template v-if="detail">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="结算单号">{{ detail.no }}</el-descriptions-item>
        <el-descriptions-item label="店铺">{{ detail.shopName }}</el-descriptions-item>
        <el-descriptions-item label="状态"
          ><el-tag :type="statusMeta(detail.status).type">{{
            statusMeta(detail.status).label
          }}</el-tag></el-descriptions-item
        >
        <el-descriptions-item label="订单实付"
          >¥{{ fenToYuan(detail.orderPayAmount) }}</el-descriptions-item
        >
        <el-descriptions-item label="退款金额"
          >¥{{ fenToYuan(detail.refundAmount) }}</el-descriptions-item
        >
        <el-descriptions-item label="店铺应结"
          >¥{{ fenToYuan(detail.settlementAmount) }}</el-descriptions-item
        >
        <el-descriptions-item label="审核备注" :span="3">{{
          detail.auditRemark || '—'
        }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detail.orders || []" class="mt-16px" max-height="420">
        <el-table-column label="订单号" min-width="180" prop="orderNo" />
        <el-table-column label="完成时间" min-width="170"
          ><template #default="{ row }">{{
            formatDate(row.orderFinishTime)
          }}</template></el-table-column
        >
        <el-table-column align="right" label="实付"
          ><template #default="{ row }">¥{{ fenToYuan(row.payAmount) }}</template></el-table-column
        >
        <el-table-column align="right" label="退款"
          ><template #default="{ row }"
            >¥{{ fenToYuan(row.refundAmount) }}</template
          ></el-table-column
        >
        <el-table-column align="right" label="平台佣金"
          ><template #default="{ row }"
            >¥{{ fenToYuan(row.platformCommissionAmount) }}</template
          ></el-table-column
        >
        <el-table-column align="right" label="应结"
          ><template #default="{ row }"
            >¥{{ fenToYuan(row.settlementAmount) }}</template
          ></el-table-column
        >
      </el-table>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ElMessageBox, FormInstance, FormRules } from 'element-plus'
import * as SettlementApi from '@/api/mall/trade/settlement'
import * as ProductShopApi from '@/api/mall/product/shop'
import { fenToYuan } from '@/utils'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'TradeShopSettlement' })

const message = useMessage()
const loading = ref(false)
const total = ref(0)
const list = ref<SettlementApi.ShopSettlementVO[]>([])
const shopList = ref<ProductShopApi.ShopVO[]>([])
const queryFormRef = ref<FormInstance>()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  shopId: undefined,
  status: undefined,
  createTime: [] as string[]
})

const statusOptions = [
  { value: 0, label: '待审核', type: 'warning' as const },
  { value: 10, label: '已审核', type: 'primary' as const },
  { value: 20, label: '已结算', type: 'success' as const },
  { value: 30, label: '已驳回', type: 'danger' as const }
]
const statusMeta = (status: number) =>
  statusOptions.find((item) => item.value === status) || { label: '未知', type: 'info' as const }
const pageSummary = computed(() =>
  list.value.reduce(
    (sum, item) => ({
      pay: sum.pay + item.orderPayAmount / 100,
      refund: sum.refund + item.refundAmount / 100,
      commission: sum.commission + item.platformCommissionAmount / 100,
      settlement: sum.settlement + item.settlementAmount / 100
    }),
    { pay: 0, refund: 0, commission: 0, settlement: 0 }
  )
)

const getList = async () => {
  loading.value = true
  try {
    const data = await SettlementApi.getShopSettlementPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const generateVisible = ref(false)
const generateLoading = ref(false)
const generateFormRef = ref<FormInstance>()
const generateForm = reactive({
  shopId: undefined as number | undefined,
  period: [] as number[],
  commissionPercent: 5
})
const generateRules: FormRules = {
  shopId: [{ required: true, message: '请选择结算店铺', trigger: 'change' }],
  period: [{ required: true, type: 'array', min: 2, message: '请选择结算周期', trigger: 'change' }],
  commissionPercent: [{ required: true, message: '请输入平台佣金比例', trigger: 'blur' }]
}
const openGenerateDialog = () => {
  Object.assign(generateForm, { shopId: undefined, period: [], commissionPercent: 5 })
  generateVisible.value = true
}
const submitGenerate = async () => {
  await generateFormRef.value?.validate()
  generateLoading.value = true
  try {
    await SettlementApi.generateShopSettlement({
      shopId: generateForm.shopId,
      periodStartTime: Number(generateForm.period[0]),
      periodEndTime: Number(generateForm.period[1]),
      commissionRate: Math.round(generateForm.commissionPercent * 100)
    })
    message.success('结算单生成成功')
    generateVisible.value = false
    await getList()
  } finally {
    generateLoading.value = false
  }
}

const detailVisible = ref(false)
const detail = ref<SettlementApi.ShopSettlementVO>()
const openDetail = async (id: number) => {
  detail.value = await SettlementApi.getShopSettlement(id)
  detailVisible.value = true
}
const handleAudit = async (row: SettlementApi.ShopSettlementVO, approved: boolean) => {
  const { value } = await ElMessageBox.prompt(
    approved ? '请输入审核备注（可选）' : '请输入驳回原因',
    approved ? '审核通过' : '驳回结算单',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputValidator: (text) => approved || !!text || '驳回原因不能为空'
    }
  )
  await SettlementApi.auditShopSettlement({ id: row.id, approved, auditRemark: value })
  message.success(approved ? '审核通过' : '结算单已驳回，订单已释放')
  await getList()
}
const handleConfirm = async (row: SettlementApi.ShopSettlementVO) => {
  await ElMessageBox.confirm(`确认店铺“${row.shopName}”已完成结算付款？`, '确认结算', {
    type: 'warning'
  })
  await SettlementApi.confirmShopSettlement(row.id)
  message.success('结算已确认')
  await getList()
}

onMounted(async () => {
  shopList.value = await ProductShopApi.getShopList()
  await getList()
})
</script>
