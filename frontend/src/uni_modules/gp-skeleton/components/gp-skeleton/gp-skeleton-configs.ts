export interface SkeletonConfigsInterface {
  padding?: string;
  gridRows?: number;
  gridColumns?: number;
  gridRowsGap?: string;
  gridColumnsGap?: string;
  itemDirection?: 'row' | 'column';
  itemGap?: string;
  itemAlign?: 'center' | 'flex-start' | 'flex-end' | 'baseline';
  headShow?: boolean;
  headWidth?: string;
  headHeight?: string;
  headBorderRadius?: string;
  textShow?: boolean;
  textRows?: number;
  textRowsGap?: string;
  textWidth?: string | string[];
  textHeight?: string | string[];
  textBorderRadius?: string;
}

export const DEFAULT_CONFIGS: SkeletonConfigsInterface = {};

export const createSkeletonConfigs = () => {
  // Banner骨架屏配置
  const bannerConfigs = (configs: SkeletonConfigsInterface = {}): SkeletonConfigsInterface => {
    return {
      padding: '30rpx',
      gridRows: 1,
      gridColumns: 1,
      gridRowsGap: '40rpx',
      gridColumnsGap: '24rpx',
      itemDirection: 'row',
      itemGap: '30rpx',
      itemAlign: 'center',
      headShow: true,
      headWidth: '100%',
      headHeight: '300rpx',
      headBorderRadius: '20rpx',
      textShow: false,
      textRows: 3,
      textRowsGap: '20rpx',
      textWidth: '100%',
      textHeight: '30rpx',
      textBorderRadius: '6rpx',
      ...configs
    };
  };
  
  // 个人信息骨架屏配置
  const infoConfigs = (configs: SkeletonConfigsInterface = {}): SkeletonConfigsInterface => {
    return {
      padding: '30rpx',
      gridRows: 1,
      gridColumns: 1,
      gridRowsGap: '50rpx',
      gridColumnsGap: '24rpx',
      itemDirection: 'row',
      itemGap: '30rpx',
      itemAlign: 'flex-start',
      headShow: true,
      headWidth: '100rpx',
      headHeight: '100rpx',
      headBorderRadius: '50%',
      textShow: true,
      textRows: 4,
      textRowsGap: '30rpx',
      textWidth: ['50%', '100%', '100%', '80%'],
      textHeight: ['40rpx', '24rpx', '24rpx', '24rpx'],
      textBorderRadius: '6rpx',
      ...configs
    };
  };
  
  // 文本段落骨架屏配置
  const textConfigs = (configs: SkeletonConfigsInterface = {}): SkeletonConfigsInterface => {
    return {
      padding: '30rpx',
      gridRows: 1,
      gridColumns: 1,
      gridRowsGap: '50rpx',
      gridColumnsGap: '24rpx',
      itemDirection: 'row',
      itemGap: '30rpx',
      itemAlign: 'flex-start',
      headShow: false,
      headWidth: '100rpx',
      headHeight: '100rpx',
      headBorderRadius: '50%',
      textShow: true,
      textRows: 4,
      textRowsGap: '30rpx',
      textWidth: ['50%', '100%', '100%', '80%'],
      textHeight: '30rpx',
      textBorderRadius: '6rpx',
      ...configs
    };
  };
  
  // 菜单骨架屏配置
  const menuConfigs = (configs: SkeletonConfigsInterface = {}): SkeletonConfigsInterface => {
    return {
      padding: '30rpx',
      gridRows: 2,
      gridColumns: 4,
      gridRowsGap: '40rpx',
      gridColumnsGap: '40rpx',
      itemDirection: 'column',
      itemGap: '16rpx',
      itemAlign: 'center',
      headShow: true,
      headWidth: '100rpx',
      headHeight: '100rpx',
      headBorderRadius: '50%',
      textShow: true,
      textRows: 1,
      textRowsGap: '0rpx',
      textWidth: '100%',
      textHeight: '24rpx',
      textBorderRadius: '6rpx',
      ...configs
    };
  };
  
  // 列表骨架屏配置
  const listConfigs = (configs: SkeletonConfigsInterface = {}): SkeletonConfigsInterface => {
    return {
      padding: '30rpx',
      gridRows: 2,
      gridColumns: 1,
      gridRowsGap: '50rpx',
      gridColumnsGap: '24rpx',
      itemDirection: 'row',
      itemGap: '30rpx',
      itemAlign: 'flex-start',
      headShow: true,
      headWidth: '200rpx',
      headHeight: '200rpx',
      headBorderRadius: '16rpx',
      textShow: true,
      textRows: 4,
      textRowsGap: '30rpx',
      textWidth: ['50%', '100%', '100%', '80%'],
      textHeight: ['38rpx', '24rpx', '24rpx', '24rpx'],
      textBorderRadius: '6rpx',
      ...configs
    };
  };
  
  // 瀑布流骨架屏配置
  const waterfallConfigs = (configs: SkeletonConfigsInterface = {}): SkeletonConfigsInterface => {
    return {
      padding: '30rpx',
      gridRows: 2,
      gridColumns: 2,
      gridRowsGap: '40rpx',
      gridColumnsGap: '24rpx',
      itemDirection: 'column',
      itemGap: '16rpx',
      itemAlign: 'center',
      headShow: true,
      headWidth: '100%',
      headHeight: '400rpx',
      headBorderRadius: '12rpx',
      textShow: true,
      textRows: 3,
      textRowsGap: '12rpx',
      textWidth: ['40%', '85%', '60%'],
      textHeight: ['30rpx', '20rpx', '20rpx'],
      textBorderRadius: '6rpx',
      ...configs
    };
  };
  
  return {
    bannerConfigs,
    infoConfigs,
    textConfigs,
    menuConfigs,
    listConfigs,
    waterfallConfigs
  };
};