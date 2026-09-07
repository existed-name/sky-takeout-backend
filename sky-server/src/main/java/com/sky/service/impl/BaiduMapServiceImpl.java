package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.enumeration.BaiduMapAPIStatus;
import com.sky.exception.baiduMap.AddressBlankException;
import com.sky.exception.baiduMap.BaiduMapStatusException;
import com.sky.external.DirectionDTO;
import com.sky.external.GeoCodingDTO;
import com.sky.service.BaiduMapService;
import com.sky.utils.BaiduMapUtil;
import org.springframework.stereotype.Service;

@Service
public class BaiduMapServiceImpl implements BaiduMapService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final BaiduMapUtil baiduMapUtil;


    // ==================== 构造器 ====================
    public BaiduMapServiceImpl( BaiduMapUtil baiduMapUtil ){
        this.baiduMapUtil = baiduMapUtil;
    }



    // ==================== 公有方法 ====================
    @Override
    public GeoCodingDTO geocode( String address ){
        /*
            检验地址有效性
            首先由于该功能要供其他模块调用、需要 1 次基础格式检验（判空
            然后就是地址是否符合规则、是否真实存在
            这里就直接交给百度地图判断、根据响应信息输出业务异常
         */
        if ( address == null || address.isBlank() ){
            throw new AddressBlankException( MessageConstant.ADDRESS_IS_BLANK );
        }
        GeoCodingDTO geoCodingDTO = baiduMapUtil.geoCoding( address );
        if ( BaiduMapAPIStatus.GEO_CODING_OK.getStatusCode() != geoCodingDTO.getStatus() ){
            // 这里直接把百度地图响应异常信息原路返回, TODO: 映射为更友好清晰的业务信息
            throw new BaiduMapStatusException( geoCodingDTO.getMsg() );
        }
        return geoCodingDTO;
    }

    @Override
    public DirectionDTO listDistances( String originAddress, String destinationAddress ){
        /*
            * 如果直接交给 baiduMapUtil 会缺失地址检验、但我又不想重复写判断
              所以直接调用 geocode 方法拿到每个地点的地理坐标数据,
            * 然后 baiduMapUtil 还没有提供参数为 GeoCodingDTO 的方法
              我又不想暴露太多细节到 service
              所以又新增了参数为 GeoCodingDTO 的方法
         */
        GeoCodingDTO originGeoCodingDto = geocode( originAddress );
        GeoCodingDTO destinationGeoCodingDto = geocode( destinationAddress );
        DirectionDTO directionDTO = baiduMapUtil.directByGeoCodingDTOs( originGeoCodingDto, destinationGeoCodingDto );
        if ( BaiduMapAPIStatus.DIRECTION_OK.getStatusCode() != directionDTO.getStatus() ){
            throw new BaiduMapStatusException( directionDTO.getMessage() );
        }
        return directionDTO;
    }


    // ==================== 私有辅助方法 ====================

}
