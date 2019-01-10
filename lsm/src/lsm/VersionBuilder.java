package lsm;

import lsm.internal.VersionEdit;

/**
 * 建造version对象
 * @author bird
 *
 */
public interface VersionBuilder {
	/**
	 * 将一个versionEdit应用到builder中，以供后续构造version
	 * @param edit
	 */
	public void apply(VersionEdit edit);
	/**
	 * 根据apply的edit信息，产生一个version对象
	 * @return
	 */
	public Version build();
}
