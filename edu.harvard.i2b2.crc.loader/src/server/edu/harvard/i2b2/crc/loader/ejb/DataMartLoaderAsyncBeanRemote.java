package edu.harvard.i2b2.crc.loader.ejb;

import javax.ejb.Remote;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType;

@Remote
public interface DataMartLoaderAsyncBeanRemote extends IDataMartLoaderBean {
	
}