package com.xumou.demo.test.spring.database;

import java.math.BigDecimal;
import lombok.Data;
import java.util.Date;
import com.xumou.demo.test.spring.database.SqlUtils;

/**
 * 用户信息
 */
@Data
@SqlUtils.Alias("TBL_USER")
public class TblUser {
	/**
	 *
	 */
	@SqlUtils.Alias("ID")
	private Integer id;
	/**
	 * 名称
	 */
	@SqlUtils.Alias("NAME")
	private String name;
	/**
	 * 订单号
	 */
	@SqlUtils.Alias("ORDER_NO")
	private Long orderNo;
	/**
	 * 金额
	 */
	@SqlUtils.Alias("MONEY")
	private Double money;
	/**
	 * 真实金额
	 */
	@SqlUtils.Alias("REAL_MONEY")
	private BigDecimal realMoney;
	/**
	 * 支付金额
	 */
	@SqlUtils.Alias("PAY_MONEY")
	private BigDecimal payMoney;
	/**
	 *
	 */
	@SqlUtils.Alias("BIRTHDAY")
	private Date birthday;
	/**
	 *
	 */
	@SqlUtils.Alias("DATE")
	private Date date;
	/**
	 *
	 */
	@SqlUtils.Alias("TIME")
	private Date time;
}
