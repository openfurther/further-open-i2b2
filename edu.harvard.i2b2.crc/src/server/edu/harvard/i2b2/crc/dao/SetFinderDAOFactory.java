package edu.harvard.i2b2.crc.dao;

import edu.harvard.i2b2.crc.dao.setfinder.IPatientSetCollectionDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryResultInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryMasterDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryRequestDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryResultTypeDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryStatusTypeDao;
import edu.harvard.i2b2.crc.dao.setfinder.IXmlResultDao;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;

public interface SetFinderDAOFactory {
   public IQueryRequestDao getQueryRequestDAO();
   public IQueryMasterDao getQueryMasterDAO();
   public IQueryInstanceDao getQueryInstanceDAO();
   public IQueryResultInstanceDao getPatientSetResultDAO();
   public IPatientSetCollectionDao getPatientSetCollectionDAO();
   public IXmlResultDao getXmlResultDao();
   public IQueryStatusTypeDao getQueryStatusTypeDao();
   public IQueryResultTypeDao getQueryResultTypeDao();
   public DataSourceLookup getDataSourceLookup();
}
