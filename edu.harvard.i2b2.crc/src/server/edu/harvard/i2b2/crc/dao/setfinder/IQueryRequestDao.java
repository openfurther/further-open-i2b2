package edu.harvard.i2b2.crc.dao.setfinder;

import edu.harvard.i2b2.common.exception.I2B2DAOException;

public interface IQueryRequestDao {

	/**
	 * Function to execute the given setfinder sql
	 * And creates query instance and query result instance
	 * @param generatedSql
	 * @param queryInstanceId
	 * @return query result instance id
	 * @throws I2B2DAOException
	 */
	//public String getPatientCount(String generatedSql, String queryInstanceId,
	//		String patientSetId) throws I2B2DAOException;

	/**
	 * Function to build sql from given query definition
	 * This function uses QueryToolUtil class to build sql
	 * @param queryRequestXml
	 * @return sql string
	 * @throws I2B2DAOException
	 */
	public String buildSql(String queryRequestXml) throws I2B2DAOException;

}