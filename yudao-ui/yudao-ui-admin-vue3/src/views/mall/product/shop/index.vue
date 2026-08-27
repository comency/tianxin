<template>
  <ContentWrap>
    <div class="mb-15px">
      <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['product:shop:create']">
        <Icon icon="ep:plus" class="mr-5px" /> 新增企业店铺
      </el-button>
      <el-button @click="getList"><Icon icon="ep:refresh" class="mr-5px" /> 刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="店铺编号" prop="id" width="100" />
      <el-table-column label="企业编号" prop="enterpriseId" width="110" />
      <el-table-column label="店铺名称" prop="name" min-width="180" />
      <el-table-column label="Logo" align="center" width="80">
        <template #default="scope">
          <img v-if="scope.row.logoUrl" :src="scope.row.logoUrl" alt="店铺 Logo" class="h-30px" />
        </template>
      </el-table-column>
      <el-table-column label="联系人" prop="contactName" width="110" />
      <el-table-column label="联系电话" prop="contactMobile" width="130" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180" :formatter="dateFormatter" />
      <el-table-column label="操作" align="center" width="150">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['product:shop:update']">
            编辑
          </el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['product:shop:delete']">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <ShopForm ref="formRef" @success="getList" />
</template>

<script lang="ts" setup>
import { DICT_TYPE } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as ProductShopApi from '@/api/mall/product/shop'
import ShopForm from './ShopForm.vue'

defineOptions({ name: 'ProductShop' })

const loading = ref(true)
const list = ref<ProductShopApi.ShopVO[]>([])
const formRef = ref()
const message = useMessage()
const { t } = useI18n()

const getList = async () => {
  loading.value = true
  try {
    list.value = await ProductShopApi.getShopList()
  } finally {
    loading.value = false
  }
}

const openForm = (type: string, id?: number) => formRef.value.open(type, id)

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await ProductShopApi.deleteShop(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(getList)
</script>
