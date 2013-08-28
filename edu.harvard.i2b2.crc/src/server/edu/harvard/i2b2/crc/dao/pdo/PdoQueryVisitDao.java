/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import oracle.sql.ArrayDescriptor;

import org.jboss.resource.adapter.jdbc.WrappedConnection;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.pdo.input.PatientListTypeHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.SQLServerFactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.VisitListTypeHandler;
import edu.harvard.i2b2.crc.dao.pdo.output.VisitFactRelated;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.EventSet;
import edu.harvard.i2b2.crc.datavo.pdo.EventType;
import edu.harvard.i2b2.crc.datavo.pdo.query.EventListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;

/**
 * Class to support visit/event section of plain pdo query $Id:
 * PdoQueryVisitDao.java,v 1.12 2008/04/08 19:41:30 rk903 Exp $
 * 
 * @author rkuttan
 */
public class PdoQueryVisitDao extends CRCDAO implements IPdoQueryVisitDao {

	private DataSourceLookup dataSourceLookup = null;

	public PdoQueryVisitDao(DataSourceLookup dataSourceLookup,
			DataSource dataSource) {
		setDbSchemaName(dataSourceLookup.getFullSchema());
		setDataSource(dataSource);
		this.dataSourceLookup = dataSourceLookup;
	}

	/**
	 * Function to return list of eventset for given encounter number list
	 * 
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @exception I2B2DAOException
	 */
	public EventSet getVisitsByEncounterNum(List<String> encounterNumList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException {
		EventSet visitDimensionSet = new EventSet();
		log.debug("visit list size " + encounterNumList.size());
		Connection conn = null;
		PreparedStatement query = null;
		try {
			conn = getDataSource().getConnection();
			VisitFactRelated visitRelated = new VisitFactRelated(
					buildOutputOptionType(detailFlag, blobFlag, statusFlag));
			String selectClause = visitRelated.getSelectClause();
			String serverType = dataSourceLookup.getServerType();
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				oracle.jdbc.driver.OracleConnection conn1 = (oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn)
						.getUnderlyingConnection();
				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "visit_dimension visit WHERE visit.encounter_num IN (SELECT * FROM TABLE (cast (? as QT_PDO_QRY_STRING_ARRAY)))";
				log.debug("Executing sql[" + finalSql + "]");
				query = conn1.prepareStatement(finalSql);

				ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
						"QT_PDO_QRY_STRING_ARRAY", conn1);
				oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
						encounterNumList.toArray(new String[] {}));
				query.setArray(1, paramArray);
			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) {
				log.debug("creating temp table");
				java.sql.Statement tempStmt = conn.createStatement();

				try {
					tempStmt
							.executeUpdate("drop table "
									+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE);
				} catch (SQLException sqlex) {
					
				}

				uploadTempTable(tempStmt, encounterNumList);
				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "visit_dimension visit WHERE visit.encounter_num IN (select distinct input_id FROM "
						+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
						+ ") order by encounter_num";
				log.debug("Executing [" + finalSql + "]");

				query = conn.prepareStatement(finalSql);

			}
			ResultSet resultSet = query.executeQuery();
			I2B2PdoFactory.EventBuilder eventBuilder = new I2B2PdoFactory().new EventBuilder(
					detailFlag, blobFlag, statusFlag);
			while (resultSet.next()) {
				EventType visitDimensionType = eventBuilder
						.buildEventSet(resultSet);
				visitDimensionSet.getEvent().add(visitDimensionType);
			}

		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("sql exception", sqlEx);
		} catch (IOException ioex) {
			log.error("", ioex);
			throw new I2B2DAOException("io exception", ioex);
		} finally {
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				deleteTempTable(conn);
			}
			try {
				JDBCUtil.closeJdbcResource(null, query, conn);
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		}
		return visitDimensionSet;
	}

	/**
	 * Get visit dimension data base on visit list
	 * (InputOptionList.getVisitListType())
	 * 
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return I2B2DAOException
	 * @throws Exception
	 */
	public EventSet getVisitDimensionSetFromVisitList(
			EventListType visitListType, boolean detailFlag, boolean blobFlag,
			boolean statusFlag) throws I2B2DAOException {
		VisitListTypeHandler visitListTypeHandler = new VisitListTypeHandler(
				dataSourceLookup, visitListType);
		String inSqlClause = visitListTypeHandler.generateWhereClauseSql();
		VisitFactRelated visitRelated = new VisitFactRelated(
				buildOutputOptionType(detailFlag, blobFlag, statusFlag));
		String selectClause = visitRelated.getSelectClause();

		String mainSqlString = " SELECT " + selectClause + "  FROM "
				+ getDbSchemaName()
				+ "visit_dimension visit WHERE visit.encounter_num IN ( ";
		mainSqlString += inSqlClause;
		mainSqlString += " )\n";

		EventSet visitDimensionSet = new EventSet();
		Connection conn = null;
		PreparedStatement preparedStmt = null;
		try {
			// execute fullsql
			conn = getDataSource().getConnection();

			log.debug("Executing Sql[" + mainSqlString + "]");

			if (visitListTypeHandler.isCollectionId()) {
				String patientEncCollectionId = visitListTypeHandler
						.getCollectionId();
				preparedStmt = conn.prepareStatement(mainSqlString);
				preparedStmt.setString(1, patientEncCollectionId);

			} else if (visitListTypeHandler.isEnumerationSet()) {
				String serverType = dataSourceLookup.getServerType();

				if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
					oracle.jdbc.driver.OracleConnection conn1 = (oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn)
							.getUnderlyingConnection();
					ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
							"QT_PDO_QRY_STRING_ARRAY", conn1);
					oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc,
							conn1, visitListTypeHandler.getEnumerationList()
									.toArray(new String[] {}));
					preparedStmt = conn1.prepareStatement(mainSqlString);
					preparedStmt.setArray(1, paramArray);
				} else if (serverType
						.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) {
					log.debug("creating temp table");
					java.sql.Statement tempStmt = conn.createStatement();
					uploadTempTable(tempStmt, visitListTypeHandler
							.getEnumerationList());
					preparedStmt = conn.prepareStatement(mainSqlString);
				}
			} else {
				preparedStmt = conn.prepareStatement(mainSqlString);
			}

			ResultSet resultSet = preparedStmt.executeQuery();
			I2B2PdoFactory.EventBuilder eventBuilder = new I2B2PdoFactory().new EventBuilder(
					detailFlag, blobFlag, statusFlag);
			while (resultSet.next()) {
				// VisitDimensionType visitDimensionType =
				// getVisitDimensionType(resultSet);
				EventType visitDimensionType = eventBuilder
						.buildEventSet(resultSet);
				visitDimensionSet.getEvent().add(visitDimensionType);
			}
		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("sql exception", sqlEx);
		} catch (IOException ioEx) {
			log.error("", ioEx);
			throw new I2B2DAOException("io exception", ioEx);
		} finally {
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				deleteTempTable(conn);
			}
			try {
				JDBCUtil.closeJdbcResource(null, preparedStmt, conn);
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		}

		return visitDimensionSet;
	}

	/**
	 * Get visit dimension from patientlist (InputOptionList.getPatientList())
	 * 
	 * @param patientListType
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientDataType.VisitDimensionSet
	 * @throws I2B2DAOException
	 */
	public EventSet getVisitDimensionSetFromPatientList(
			PatientListType patientListType, boolean detailFlag,
			boolean blobFlag, boolean statusFlag) throws I2B2DAOException {

		VisitFactRelated visitRelated = new VisitFactRelated(
				buildOutputOptionType(detailFlag, blobFlag, statusFlag));
		String selectClause = visitRelated.getSelectClause();
		String mainSqlString = " SELECT " + selectClause + "  FROM "
				+ getDbSchemaName()
				+ "visit_dimension visit WHERE visit.patient_num IN ( ";
		PatientListTypeHandler patientListTypeHandler = new PatientListTypeHandler(
				dataSourceLookup, patientListType);
		String inSqlClause = patientListTypeHandler.generateWhereClauseSql();
		mainSqlString += inSqlClause;
		mainSqlString += " )\n";

		log.debug("Executing sql[" + mainSqlString + "]");
		EventSet visitDimensionSet = new EventSet();
		Connection conn = null;
		PreparedStatement preparedStmt = null;
		try {
			// execute fullsql
			conn = getDataSource().getConnection();

			if (patientListTypeHandler.isCollectionId()) {
				preparedStmt = conn.prepareStatement(mainSqlString);
				preparedStmt.setInt(1, Integer.parseInt(patientListTypeHandler
						.getCollectionId()));
			} else if (patientListTypeHandler.isEnumerationSet()) {
				String serverType = dataSourceLookup.getServerType();
				if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
					oracle.jdbc.driver.OracleConnection conn1 = (oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn)
							.getUnderlyingConnection();

					ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
							"QT_PDO_QRY_STRING_ARRAY", conn1);
					oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc,
							conn1, patientListTypeHandler
									.getIntListFromPatientNumList().toArray(
											new String[] {}));
					preparedStmt = conn1.prepareStatement(mainSqlString);
					preparedStmt.setArray(1, paramArray);

				} else if (serverType
						.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) {
					log.debug("creating temp table");
					java.sql.Statement tempStmt = conn.createStatement();
					uploadTempTable(tempStmt, patientListTypeHandler
							.getIntListFromPatientNumList());
					preparedStmt = conn.prepareStatement(mainSqlString);
				}

			} else if (patientListTypeHandler.isEntireSet()) {
				// log.debug("No need to pass parameter to sql");
				preparedStmt = conn.prepareStatement(mainSqlString);
			}

			ResultSet resultSet = preparedStmt.executeQuery();
			I2B2PdoFactory.EventBuilder eventBuilder = new I2B2PdoFactory().new EventBuilder(
					detailFlag, blobFlag, statusFlag);
			while (resultSet.next()) {
				// VisitDimensionType visitDimensionType =
				// getVisitDimensionType(resultSet);
				EventType visitDimensionType = eventBuilder
						.buildEventSet(resultSet);
				visitDimensionSet.getEvent().add(visitDimensionType);
			}

		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("sql exception", sqlEx);
		} catch (IOException ioEx) {
			log.error("", ioEx);
			throw new I2B2DAOException("io exception", ioEx);
		} finally {
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				deleteTempTable(conn);
			}
			try {
				JDBCUtil.closeJdbcResource(null, preparedStmt, conn);
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		}
		return visitDimensionSet;
	}

	private void uploadTempTable(Statement tempStmt, List<String> patientNumList)
			throws SQLException {
		String createTempInputListTable = "create table "
				+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
				+ " ( input_id varchar(100) )";
		tempStmt.executeUpdate(createTempInputListTable);
		log.debug("created temp table"
				+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE);
		// load to temp table
		// TempInputListInsert inputListInserter = new
		// TempInputListInsert(dataSource,TEMP_PDO_INPUTLIST_TABLE);
		// inputListInserter.setBatchSize(100);
		int i = 0;
		for (String singleValue : patientNumList) {
			tempStmt.addBatch("insert into "
					+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
					+ " values ('" + singleValue + "' )");
			log.debug("adding batch" + singleValue);
			i++;
			if (i % 100 == 0) {
				log.debug("batch insert");
				tempStmt.executeBatch();

			}
		}
		log.debug("batch insert1");
		tempStmt.executeBatch();
	}

	private void deleteTempTable(Connection conn) {

		Statement deleteStmt = null;
		try {
			deleteStmt = conn.createStatement();
			conn
					.createStatement()
					.executeUpdate(
							"drop table "
									+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE);
		} catch (SQLException sqle) {
			
		} finally {
			try {
				deleteStmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
