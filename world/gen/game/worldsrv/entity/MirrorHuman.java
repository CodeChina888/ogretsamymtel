package game.worldsrv.entity;

import core.db.DBConsts;
import core.Port;
import core.Record;
import game.worldsrv.entity.Unit;
import core.gen.GofGenFile;

@GofGenFile
public abstract class MirrorHuman extends Unit {
	public MirrorHuman() {
		super();
		setTitleSn(0);
	}

	public MirrorHuman(Record record) {
		super(record);
	}
	
	/**
	 * 属性关键字
	 */
	public static class SuperK {
		public static final String id = "id";	//id
		public static final String PartnerLineup = "PartnerLineup";	//伙伴阵容(id列表) 
		public static final String PartnerStance = "PartnerStance";	//伙伴站位(0-W型；1-M型)
		public static final String InstallGodsJSON = "InstallGodsJSON";	//上阵爆点技能sn,lv,power,value
		public static final String TitleSn = "TitleSn";	//称号sn
		public static final String TitleShow = "TitleShow";	//是否显示称号
	}

	/**
	 * id
	 */
	public long getId() {
		return record.get("id");
	}

	public void setId(final long id) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("id", id);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 伙伴阵容(id列表) 
	 */
	public String getPartnerLineup() {
		return record.get("PartnerLineup");
	}

	public void setPartnerLineup(final String PartnerLineup) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("PartnerLineup", PartnerLineup);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 伙伴站位(0-W型；1-M型)
	 */
	public int getPartnerStance() {
		return record.get("PartnerStance");
	}

	public void setPartnerStance(final int PartnerStance) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("PartnerStance", PartnerStance);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 上阵爆点技能sn,lv,power,value
	 */
	public String getInstallGodsJSON() {
		return record.get("InstallGodsJSON");
	}

	public void setInstallGodsJSON(final String InstallGodsJSON) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("InstallGodsJSON", InstallGodsJSON);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 称号sn
	 */
	public int getTitleSn() {
		return record.get("TitleSn");
	}

	public void setTitleSn(final int TitleSn) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TitleSn", TitleSn);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 是否显示称号
	 */
	public boolean isTitleShow() {
		return record.<Integer>get("TitleShow") == 1;
	}

	public void setTitleShow(boolean TitleShow) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TitleShow", TitleShow ? 1 : 0);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}

}