<template>
  <div class="address-management">
    <h2 class="page-title">收货地址管理</h2>

    <!-- 地址列表 -->
    <div v-if="addresses.length > 0" class="address-list">
      <div
        v-for="address in addresses"
        :key="address.addressId"
        class="address-item"
        :class="{ 'default-address': address.isDefault }"
      >
        <div class="address-header">
          <span class="receiver-name">{{ address.receiverName }}</span>
          <span class="receiver-phone">{{ address.receiverPhone }}</span>
          <span v-if="address.isDefault" class="default-tag">默认</span>
        </div>

        <div class="address-detail">
          {{ address.receiverProvince }}{{ address.receiverCity }}{{ address.receiverRegion }}
          {{ address.receiverDetailAddress }}
          <span v-if="address.receiverPostCode">({{ address.receiverPostCode }})</span>
        </div>

        <div class="address-actions">
          <el-button size="small" @click="editAddress(address)">编辑</el-button>
          <el-button
            size="small"
            @click="setDefaultAddress(address.addressId)"
            :disabled="address.isDefault"
          >
            设为默认
          </el-button>
          <el-button size="small" type="danger" @click="deleteAddress(address.addressId)">
            删除
          </el-button>
        </div>
      </div>
    </div>

    <div v-else class="empty-address">
      <el-empty description="暂无收货地址" />
      <el-button type="primary" @click="showAddForm = true" style="margin-top: 20px">
        添加新地址
      </el-button>
    </div>

    <!-- 添加/编辑地址表单 -->
    <el-dialog
      :title="editingAddress ? '编辑地址' : '添加地址'"
      v-model="showAddForm"
      width="500px"
      @close="resetForm"
    >
      <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-width="80px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="所在地区" required>
          <el-cascader
            v-model="regionCascader"
            :options="regionOptions"
            :props="{ checkStrictly: true }"
            @change="handleRegionChange"
            placeholder="请选择省/市/区"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="详细地址" prop="receiverDetailAddress">
          <el-input
            v-model="addressForm.receiverDetailAddress"
            type="textarea"
            placeholder="街道、楼牌号等"
            :rows="3"
          />
        </el-form-item>

        <el-form-item label="邮政编码" prop="receiverPostCode">
          <el-input v-model="addressForm.receiverPostCode" placeholder="选填" />
        </el-form-item>

        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAddForm = false">取消</el-button>
        <el-button type="primary" @click="submitAddress" :loading="submitLoading">
          保存地址
        </el-button>
      </template>
    </el-dialog>

    <div v-if="addresses.length > 0" class="add-address-btn">
      <el-button type="primary" @click="showAddForm = true">添加新地址</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import {
    getAddressListApi,
    createAddressApi,
    updateAddressApi,
    deleteAddressApi,
    setDefaultAddressApi,
  } from '@/api/address'
  import type { AddressDTO, AddressCreateDTO, AddressUpdateDTO } from '@/api/address/type'
  import type { FormInstance, FormRules } from 'element-plus'

  const regionOptions = [
    {
      value: '北京市',
      label: '北京市',
      children: [
        {
          value: '北京市',
          label: '北京市',
          children: [
            { value: '东城区', label: '东城区' },
            { value: '西城区', label: '西城区' },
            { value: '朝阳区', label: '朝阳区' },
          ],
        },
      ],
    },
    {
      value: '上海市',
      label: '上海市',
      children: [
        {
          value: '上海市',
          label: '上海市',
          children: [
            { value: '黄浦区', label: '黄浦区' },
            { value: '徐汇区', label: '徐汇区' },
            { value: '长宁区', label: '长宁区' },
          ],
        },
      ],
    },
  ]

  const addresses = ref<AddressDTO[]>([])
  const showAddForm = ref(false)
  const submitLoading = ref(false)
  const editingAddress = ref<AddressDTO | null>(null)

  const addressFormRef = ref<FormInstance>()
  const addressForm = reactive({
    receiverName: '',
    receiverPhone: '',
    receiverProvince: '',
    receiverCity: '',
    receiverRegion: '',
    receiverDetailAddress: '',
    receiverPostCode: '',
    isDefault: false,
  })

  const regionCascader = ref<string[]>([])

  const addressRules = reactive<FormRules>({
    receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
    receiverPhone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
    ],
    receiverDetailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  })

  const loadAddresses = async () => {
    try {
      const res = await getAddressListApi()
      addresses.value = res

      // 触发地址更新事件，通知父组件
      if (typeof window !== 'undefined' && window.dispatchEvent) {
        window.dispatchEvent(
          new CustomEvent('addressListUpdated', {
            detail: { addresses: res },
          })
        )
      }
    } catch (error) {
      console.error('加载地址列表失败:', error)
      ElMessage.error('加载地址列表失败')
    }
  }

  const handleRegionChange = (value: string[]) => {
    if (value.length >= 3) {
      addressForm.receiverProvince = value[0]
      addressForm.receiverCity = value[1]
      addressForm.receiverRegion = value[2]
    }
  }

  // 添加/编辑地址
  const editAddress = (address: AddressDTO) => {
    editingAddress.value = address
    addressForm.receiverName = address.receiverName
    addressForm.receiverPhone = address.receiverPhone
    addressForm.receiverProvince = address.receiverProvince
    addressForm.receiverCity = address.receiverCity
    addressForm.receiverRegion = address.receiverRegion
    addressForm.receiverDetailAddress = address.receiverDetailAddress
    addressForm.receiverPostCode = address.receiverPostCode || ''
    addressForm.isDefault = address.isDefault

    // 设置级联选择器的值
    regionCascader.value = [address.receiverProvince, address.receiverCity, address.receiverRegion]

    showAddForm.value = true
  }

  // 提交地址
  const submitAddress = () => {
    addressFormRef.value?.validate(async (valid) => {
      if (valid) {
        submitLoading.value = true
        try {
          if (editingAddress.value) {
            // 编辑地址
            const updateDto: AddressUpdateDTO = {
              addressId: editingAddress.value.addressId,
              receiverName: addressForm.receiverName,
              receiverPhone: addressForm.receiverPhone,
              receiverProvince: addressForm.receiverProvince,
              receiverCity: addressForm.receiverCity,
              receiverRegion: addressForm.receiverRegion,
              receiverDetailAddress: addressForm.receiverDetailAddress,
              receiverPostCode: addressForm.receiverPostCode,
              isDefault: addressForm.isDefault,
            }
            await updateAddressApi(updateDto)
            ElMessage.success('地址更新成功')
          } else {
            // 添加地址
            const createDto: AddressCreateDTO = {
              receiverName: addressForm.receiverName,
              receiverPhone: addressForm.receiverPhone,
              receiverProvince: addressForm.receiverProvince,
              receiverCity: addressForm.receiverCity,
              receiverRegion: addressForm.receiverRegion,
              receiverDetailAddress: addressForm.receiverDetailAddress,
              receiverPostCode: addressForm.receiverPostCode,
              isDefault: addressForm.isDefault,
            }
            await createAddressApi(createDto)
            ElMessage.success('地址添加成功')
          }

          showAddForm.value = false
          loadAddresses()
        } catch (error: any) {
          ElMessage.error(error.msg || '操作失败')
        } finally {
          submitLoading.value = false
        }
      }
    })
  }

  const setDefaultAddress = async (addressId: number) => {
    try {
      await setDefaultAddressApi(addressId)
      ElMessage.success('默认地址设置成功')
      loadAddresses()
    } catch (error: any) {
      ElMessage.error(error.msg || '设置失败')
    }
  }

  const deleteAddress = async (addressId: number) => {
    ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await deleteAddressApi(addressId)
        ElMessage.success('地址删除成功')
        loadAddresses()
      } catch (error: any) {
        ElMessage.error(error.msg || '删除失败')
      }
    })
  }

  const resetForm = () => {
    editingAddress.value = null
    addressFormRef.value?.resetFields()
    regionCascader.value = []
  }

  onMounted(() => {
    loadAddresses()
  })
</script>

<style scoped>
  .address-management {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
  }

  .page-title {
    font-size: 24px;
    color: #333;
    margin-bottom: 20px;
  }

  .address-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .address-item {
    border: 1px solid #ebeef5;
    border-radius: 4px;
    padding: 15px;
    background: #fff;
  }

  .default-address {
    border-color: #e40000;
    background: #fef0f0;
  }

  .address-header {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 10px;
  }

  .receiver-name {
    font-weight: bold;
    color: #333;
    font-size: 16px;
  }

  .receiver-phone {
    color: #666;
  }

  .default-tag {
    background: #e40000;
    color: white;
    padding: 2px 6px;
    border-radius: 2px;
    font-size: 12px;
  }

  .address-detail {
    color: #666;
    line-height: 1.5;
    margin-bottom: 15px;
  }

  .address-actions {
    display: flex;
    gap: 10px;
  }

  .empty-address {
    text-align: center;
    padding: 40px 0;
  }

  .add-address-btn {
    margin-top: 20px;
    text-align: center;
  }
</style>
