<template>
  <div class="card border-left shadow h-100 py-2 text-left">
    <div class="card-body">
      <div class="row no-gutters align-items-center">
        <div class="col mr-2">
          <div class="font-weight-bold text-uppercase mb-1">{{ title }}</div>
          <div class="h5 mb-0 font-weight-bold text-dark">{{  value | number }}</div>
        </div>
        <div class="col-auto text-muted">
          <h1><b-icon :icon="icon"/></h1>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "SingleStatCard",
  props: ['title', 'value', 'icon']
}
</script>

<style scoped>

</style>
