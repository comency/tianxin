<template>
  <doc-alert title="【交易】交易订单" url="https://doc.iocoder.cn/mall/trade-order/" />
  <doc-alert title="【交易】购物车" url="https://doc.iocoder.cn/mall/trade-cart/" />

  <!-- 搜索 -->
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="68px"
    >
      <el-form-item label="订单状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-280px" clearable placeholder="全部">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.TRADE_ORDER_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="!isManagedShop" label="所属店铺" prop="shopId">
        <el-select v-model="queryParams.shopId" class="!w-280px" clearable placeholder="全部店铺">
          <el-option label="平台自营" :value="0" />
          <el-option v-for="shop in shopList" :key="shop.id" :label="shop.name" :value="shop.id!" />
        </el-select>
      </el-form-item>
      <el-form-item label="支付方式" prop="payChannelCode">
        <el-select
          v-model="queryParams.payChannelCode"
          class="!w-280px"
          clearable
          placeholder="全部"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.PAY_CHANNEL_CODE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-280px"
          end-placeholder="自定义时间"
          start-placeholder="自定义时间"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="订单来源" prop="terminal">
        <el-select v-model="queryParams.terminal" class="!w-280px" clearable placeholder="全部">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.TERMINAL)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="订单类型" prop="type">
        <el-select v-model="queryParams.type" class="!w-280px" clearable placeholder="全部">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.TRADE_ORDER_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="配送方式" prop="deliveryType">
        <el-select v-model="queryParams.deliveryType" class="!w-280px" clearable placeholder="全部">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.TRADE_DELIVERY_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="queryParams.deliveryType === DeliveryTypeEnum.EXPRESS.type"
        label="快递公司"
        prop="logisticsId"
      >
        <el-select v-model="queryParams.logisticsId" class="!w-280px" clearable placeholder="全部">
          <el-option
            v-for="item in deliveryExpressList"
            :key="item.id"
            :label="item.name"
            :value="item.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="queryParams.deliveryType === DeliveryTypeEnum.PICK_UP.type"
        label="自提门店"
        prop="pickUpStoreId"
      >
        <el-select
          v-model="queryParams.pickUpStoreId"
          class="!w-280px"
          clearable
          multiple
          placeholder="全部"
        >
          <el-option
            v-for="item in pickUpStoreList"
            :key="item.id"
            :label="item.name"
            :value="item.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="queryParams.deliveryType === DeliveryTypeEnum.PICK_UP.type"
        label="核销码"
        prop="pickUpVerifyCode"
      >
        <el-input
          v-model="queryParams.pickUpVerifyCode"
          class="!w-280px"
          clearable
          placeholder="请输入自提核销码"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="聚合搜索">
        <el-input
          v-show="true"
          v-model="queryParams[queryType.queryParam]"
          :type="queryType.queryParam === 'userId' ? 'number' : 'text'"
          class="!w-280px"
          clearable
          placeholder="请输入"
        >
          <template #prepend>
            <el-select
              v-model="queryType.queryParam"
              class="!w-110px"
              clearable
              placeholder="全部"
              @change="inputChangeSelect"
            >
              <el-option
                v-for="dict in dynamicSearchList"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 店铺经营概览：店铺负责人仅看到自己负责店铺的数据 -->
  <el-row :gutter="16" class="summary">
    <el-col :sm="6" :xs="12" v-loading="loading">
      <SummaryCard
        title="正常订单"
        icon="icon-park-outline:transaction-order"
        icon-color="bg-blue-100"
        icon-bg-color="text-blue-500"
        :value="summary?.orderCount || 0"
      />
    </el-col>
    <el-col :sm="6" :xs="12" v-loading="loading">
      <SummaryCard
        title="订单金额"
        icon="streamline:money-cash-file-dollar-common-money-currency-cash-file"
        icon-color="bg-purple-100"
        icon-bg-color="text-purple-500"
        prefix="￥"
        :decimals="2"
        :value="Number(fenToYuan(summary?.orderPayPrice || 0))"
      />
    </el-col>
    <el-col :sm="6" :xs="12" v-loading="loading">
      <SummaryCard
        title="待发货订单"
        icon="ep:box"
        icon-color="bg-orange-100"
        icon-bg-color="text-orange-500"
        :value="undeliveredTotal"
      />
    </el-col>
    <el-col :sm="6" :xs="12" v-loading="loading">
      <SummaryCard
        title="退款单数"
        icon="heroicons:receipt-refund"
        icon-color="bg-green-100"
        icon-bg-color="text-green-500"
        :value="summary?.afterSaleCount || 0"
      />
    </el-col>
  </el-row>

  <!-- 列表 -->
  <ContentWrap>
    <!-- 添加 row-key="id" 解决列数据中的 table#header 数据不刷新的问题  -->
    <el-table v-loading="loading" :data="list" row-key="id">
      <OrderTableColumn :list="list" :pick-up-store-list="pickUpStoreList">
        <template #default="{ row }">
          <div class="flex items-center justify-center">
            <el-button
              v-hasPermi="['trade:order:query']"
              link
              type="primary"
              @click="openDetail(row.id)"
            >
              <Icon icon="ep:notification" />
              详情
            </el-button>
            <el-dropdown
              v-hasPermi="['trade:order:update']"
              @command="(command) => handleCommand(command, row)"
            >
              <el-button link type="primary">
                <Icon icon="ep:d-arrow-right" />
                更多
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <!-- 如果是【快递】，并且【未发货】，则展示【发货】按钮 -->
                  <el-dropdown-item
                    v-if="
                      row.deliveryType === DeliveryTypeEnum.EXPRESS.type &&
                      row.status === TradeOrderStatusEnum.UNDELIVERED.status
                    "
                    command="delivery"
                  >
                    <Icon icon="ep:takeaway-box" />
                    发货
                  </el-dropdown-item>
                  <el-dropdown-item command="remark">
                    <Icon icon="ep:chat-line-square" />
                    备注
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </OrderTableColumn>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 各种操作的弹窗 -->
  <OrderDeliveryForm ref="deliveryFormRef" @success="getList" />
  <OrderUpdateRemarkForm ref="updateRemarkForm" @success="getList" />
</template>

<script lang="ts" setup>
import type { FormInstance } from 'element-plus'
import OrderDeliveryForm from '@/views/mall/trade/order/form/OrderDeliveryForm.vue'
import OrderUpdateRemarkForm from '@/views/mall/trade/order/form/OrderUpdateRemarkForm.vue'
import * as TradeOrderApi from '@/api/mall/trade/order'
import * as PickUpStoreApi from '@/api/mall/trade/delivery/pickUpStore'
import { DICT_TYPE, getIntDictOptions, getStrDictOptions } from '@/utils/dict'
import * as DeliveryExpressApi from '@/api/mall/trade/delivery/express'
import * as ProductShopApi from '@/api/mall/product/shop'
import { fenToYuan } from '@/utils'
import { DeliveryTypeEnum, TradeOrderStatusEnum } from '@/utils/constants'
import SummaryCard from '@/components/SummaryCard/index.vue'
import { OrderTableColumn } from './components'

defineOptions({ name: 'TradeOrder' })

const { currentRoute, push } = useRouter() // 路由跳转
const loading = ref(true) // 列表的加载中
const total = ref(2) // 列表的总页数
const list = ref<TradeOrderApi.OrderVO[]>([]) // 列表的数据
const summary = ref<TradeOrderApi.TradeOrderSummaryRespVO>() // 店铺经营统计
const undeliveredTotal = ref(0) // 同一筛选范围内待发货订单数量
const queryFormRef = ref<FormInstance>() // 搜索的表单
// 表单搜索
const queryParams = ref({
  pageNo: 1, // 页数
  pageSize: 10, // 每页显示数量
  shopId: undefined as number | undefined, // 所属企业店铺
  status: undefined, // 订单状态
  payChannelCode: undefined, // 支付方式
  createTime: undefined, // 创建时间
  terminal: undefined, // 订单来源
  type: undefined, // 订单类型
  deliveryType: undefined, // 配送方式
  logisticsId: undefined, // 快递公司
  pickUpStoreId: undefined, // 自提门店
  pickUpVerifyCode: undefined // 自提核销码
})
const queryType = reactive({ queryParam: '' }) // 订单搜索类型 queryParam

// 订单聚合搜索 select 类型配置（动态搜索）
const dynamicSearchList = ref([
  { value: 'no', label: '订单号' },
  { value: 'userId', label: '用户UID' },
  { value: 'userNickname', label: '用户昵称' },
  { value: 'userMobile', label: '用户电话' }
])
/**
 * 聚合搜索切换查询对象时触发
 * @param val
 */
const inputChangeSelect = (val: string) => {
  dynamicSearchList.value
    .filter((item) => item.value !== val)
    ?.forEach((item1) => {
      // 清除集合搜索无用属性
      if (queryParams.value.hasOwnProperty(item1.value)) {
        delete queryParams.value[item1.value]
      }
    })
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const params = unref(queryParams)
    const undeliveredParams = {
      ...params,
      pageNo: 1,
      pageSize: 1,
      status: TradeOrderStatusEnum.UNDELIVERED.status
    }
    const [summaryData, data, undeliveredData] = await Promise.all([
      TradeOrderApi.getOrderSummary(params),
      TradeOrderApi.getOrderPage(params),
      TradeOrderApi.getOrderPage(undeliveredParams)
    ])
    summary.value = summaryData
    list.value = data.list
    total.value = data.total
    undeliveredTotal.value = undeliveredData.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = async () => {
  queryParams.value.pageNo = 1
  await getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryParams.value = {
    pageNo: 1, // 页数
    pageSize: 10, // 每页显示数量
    shopId: undefined, // 所属企业店铺
    status: undefined, // 订单状态
    payChannelCode: undefined, // 支付方式
    createTime: undefined, // 创建时间
    terminal: undefined, // 订单来源
    type: undefined, // 订单类型
    deliveryType: undefined, // 配送方式
    logisticsId: undefined, // 快递公司
    pickUpStoreId: undefined, // 自提门店
    pickUpVerifyCode: undefined // 自提核销码
  }
  handleQuery()
}

/** 查看订单详情 */
const openDetail = (id: number) => {
  push({ name: 'TradeOrderDetail', params: { id } })
}

/** 操作分发 */
const deliveryFormRef = ref()
const updateRemarkForm = ref()
const handleCommand = (command: string, row: TradeOrderApi.OrderVO) => {
  switch (command) {
    case 'remark':
      updateRemarkForm.value?.open(row)
      break
    case 'delivery':
      deliveryFormRef.value?.open(row)
      break
  }
}

// 监听路由变化更新列表，解决订单保存/更新后，列表不刷新的问题。
watch(
  () => currentRoute.value,
  () => {
    getList()
  }
)

const pickUpStoreList = ref<PickUpStoreApi.DeliveryPickUpStoreVO[]>([]) // 自提门店精简列表
const deliveryExpressList = ref<DeliveryExpressApi.DeliveryExpressVO[]>([]) // 物流公司
const shopList = ref<ProductShopApi.ShopVO[]>([]) // 企业店铺
const managedShop = ref<ProductShopApi.ShopVO | null>(null) // 当前负责人绑定的店铺
const isManagedShop = computed(() => !!managedShop.value)

/** 初始化 **/
onMounted(async () => {
  managedShop.value = await ProductShopApi.getMyManagedShop()
  if (managedShop.value?.id) {
    queryParams.value.shopId = managedShop.value.id
  }
  await getList()
  pickUpStoreList.value = await PickUpStoreApi.getSimpleDeliveryPickUpStoreList()
  deliveryExpressList.value = await DeliveryExpressApi.getSimpleDeliveryExpressList()
  if (!managedShop.value) {
    shopList.value = await ProductShopApi.getShopList()
  }
})
</script>
