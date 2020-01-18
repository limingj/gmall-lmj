package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author shkstart
 * @create 2020-01-05 10:46
 */
@Data
public class BaseAttrVo extends ProductAttrValueEntity {
    public void setvalueSelected(List<String> valueSelected){
        if(!CollectionUtils.isEmpty(valueSelected)){
            this.setAttrValue(StringUtils.join(valueSelected,","));
        }else {
            this.setAttrValue(null);
        }
    }
}
