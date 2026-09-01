<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="企业编号" prop="enterpriseId">
        <el-input-number v-model="formData.enterpriseId" :min="1" class="!w-240px" />
      </el-form-item>
      <el-form-item label="店铺名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入店铺名称" />
      </el-form-item>
      <el-form-item label="店铺 Logo">
        <UploadImg v-model="formData.logoUrl" :limit="1" :is-show-tip="false" />
      </el-form-item>
      <el-form-item label="联系人">
        <el-input v-model="formData.contactName" placeholder="请输入联系人" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="formData.contactMobile" placeholder="请输入联系电话" />
      </el-form-item>
      <el-form-item label="店铺负责人">
        <UserSelectV2
          v-model="formData.managerUserId"
          placeholder="请选择后台商家账号"
          class="!w-240px"
          clearable
        />
        <div class="text-gray-400 text-12px mt-4px"
          >新订单将记录该账号，后续用于商家订单和售后的权限隔离。</div
        >
      </el-form-item>
      <el-form-item label="店铺状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="店铺简介">
        <el-input v-model="formData.introduction" type="textarea" placeholder="请输入店铺简介" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'
import * as ProductShopApi from '@/api/mall/product/shop'
import UserSelectV2 from '@/views/system/user/components/UserSelectV2.vue'

defineOptions({ name: 'ProductShopForm' })

const { t } = useI18n()
const message = useMessage()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formRef = ref()
const formData = ref<ProductShopApi.ShopVO>(createDefaultFormData())
const formRules = reactive({
  enterpriseId: [{ required: true, message: '企业编号不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '店铺名称不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '店铺状态不能为空', trigger: 'change' }]
})

function createDefaultFormData(): ProductShopApi.ShopVO {
  return {
    enterpriseId: undefined,
    name: '',
    logoUrl: '',
    contactName: '',
    contactMobile: '',
    managerUserId: undefined,
    introduction: '',
    status: CommonStatusEnum.ENABLE
  }
}

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  formData.value = createDefaultFormData()
  formRef.value?.resetFields()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ProductShopApi.getShop(id)
    } finally {
      formLoading.value = false
    }
  }
}

defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  formLoading.value = true
  try {
    if (formType.value === 'create') {
      await ProductShopApi.createShop(formData.value)
      message.success(t('common.createSuccess'))
    } else {
      await ProductShopApi.updateShop(formData.value)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>
