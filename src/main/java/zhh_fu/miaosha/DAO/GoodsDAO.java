package zhh_fu.miaosha.DAO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import zhh_fu.miaosha.pojo.Goods;
import zhh_fu.miaosha.pojo.MiaoshaGoods;
import zhh_fu.miaosha.pojo.User;
import zhh_fu.miaosha.vo.GoodsVo;

import java.util.List;

@Repository
@Mapper
public interface GoodsDAO {

    //秒杀商品列表
    @Select("select g.*,mg.miaosha_price, mg.stock_count,mg.start_date,mg.end_date " +
            "from miaosha_goods mg left join goods g on mg.goods_id = g.id")
    List<GoodsVo> getGoodsVoList();

    //商品详细信息
    @Select("select g.*,mg.miaosha_price, mg.stock_count,mg.start_date,mg.end_date " +
            "from miaosha_goods mg left join goods g on mg.goods_id = g.id where g.id = #{goodsId}")
    GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);

    @Update("update miaosha_goods set stock_count = stock_count - 1 where goods_id = #{goodsId}")
    void reduceStock(MiaoshaGoods goods);
}
