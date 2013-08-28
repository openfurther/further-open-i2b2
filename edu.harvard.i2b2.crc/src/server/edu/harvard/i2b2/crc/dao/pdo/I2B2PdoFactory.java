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
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.datavo.pdo.BlobType;
import edu.harvard.i2b2.crc.datavo.pdo.ConceptType;
import edu.harvard.i2b2.crc.datavo.pdo.EventType;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;
import edu.harvard.i2b2.crc.datavo.pdo.ObserverType;
import edu.harvard.i2b2.crc.datavo.pdo.ParamType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientIdType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientType;

/**
 * Class to build individual sections of plain pdo xml like
 * patient,concept,observationfact from the given {@link java.sql.ResultSet}
 * 
 * $Id: I2B2PdoFactory.java,v 1.15 2008/08/07 14:25:05 rk903 Exp $
 * 
 * @author rkuttan
 */
public final class I2B2PdoFactory
{
	// -----------------------------------------------------
	// $FURTHeR$ Oren Livne 5-OCT-2009
	// Added logging support
	protected final Log log = LogFactory.getLog(getClass());

	// -----------------------------------------------------

	private final DTOFactory dtoFactory = new DTOFactory();

	/**
	 * Inner class to build observation fact in Plain PDO format
	 */
	public class ObservationFactBuilder
	{
		/** detail flag **/
		boolean obsFactDetailFlag = false;
		/** blob flag **/
		boolean obsFactBlobFlag = false;
		/** status flag **/
		boolean obsFactStatusFlag = false;

		/**
		 * Parameter constructor
		 * 
		 * @param detailFlag
		 * @param blobFlag
		 * @param statusFlag
		 */
		public ObservationFactBuilder(final boolean detailFlag, final boolean blobFlag,
				final boolean statusFlag)
		{
			this.obsFactDetailFlag = detailFlag;
			this.obsFactBlobFlag = blobFlag;
			this.obsFactStatusFlag = statusFlag;
		}

		/**
		 * Read one record from resultset and build observation fact
		 * 
		 * @param rowSet
		 *            resultset
		 * @return Observation fact set
		 * @throws SQLException
		 * @throws IOException
		 */
		public ObservationType buildObservationSet(final ResultSet rowSet)
				throws SQLException, IOException
		{
			final ObservationType observationFactType = new ObservationType();
			final PatientIdType patientIdType = new PatientIdType();
			patientIdType.setValue(rowSet.getString("obs_patient_num"));

			observationFactType.setPatientId(patientIdType);
			final ObservationType.EventId eventId = new ObservationType.EventId();
			eventId.setValue(rowSet.getString("obs_encounter_num"));
			observationFactType.setEventId(eventId);
			final ObservationType.ConceptCd conceptCd = new ObservationType.ConceptCd();
			conceptCd.setValue(rowSet.getString("obs_concept_cd"));
			observationFactType.setConceptCd(conceptCd);

			final ObservationType.ModifierCd modifierCd = new ObservationType.ModifierCd();
			modifierCd.setValue(rowSet.getString("obs_modifier_cd"));
			observationFactType.setModifierCd(modifierCd);

			final Date startDate = rowSet.getTimestamp("obs_start_date");

			if (startDate != null)
			{
				observationFactType.setStartDate(dtoFactory
						.getXMLGregorianCalendar(startDate.getTime()));
			}

			final ObservationType.ObserverCd observerCd = new ObservationType.ObserverCd();
			observerCd.setValue(((rowSet.getString("obs_provider_id") != null) ? rowSet
					.getString("obs_provider_id") : ""));
			observationFactType.setObserverCd(observerCd);

			if (obsFactDetailFlag)
			{
				final Date endDate = rowSet.getTimestamp("obs_end_date");

				if (endDate != null)
				{
					observationFactType.setEndDate(dtoFactory
							.getXMLGregorianCalendar(endDate.getTime()));
				}

				observationFactType.setValuetypeCd(rowSet.getString("obs_valtype_cd"));
				observationFactType
						.setTvalChar((rowSet.getString("obs_tval_char") != null) ? rowSet
								.getString("obs_tval_char") : "");

				final ObservationType.NvalNum valNum = new ObservationType.NvalNum();
				valNum.setValue(rowSet.getBigDecimal("obs_nval_num"));
				observationFactType.setNvalNum(valNum);

				final ObservationType.ValueflagCd valueFlagCd = new ObservationType.ValueflagCd();
				valueFlagCd.setValue(rowSet.getString("obs_valueflag_cd"));
				observationFactType.setValueflagCd(valueFlagCd);

				observationFactType.setQuantityNum(rowSet
						.getBigDecimal("obs_quantity_num"));

				observationFactType.setUnitsCd(rowSet.getString("obs_units_cd"));

				final ObservationType.LocationCd locationCd = new ObservationType.LocationCd();
				locationCd.setValue(rowSet.getString("obs_location_cd"));
				observationFactType.setLocationCd(locationCd);
				observationFactType.setConfidenceNum(rowSet
						.getBigDecimal("obs_confidence_num"));
				// Double confidenceNum =
				// rowSet.getDouble("obs_confidence_num");
			}

			if (obsFactBlobFlag)
			{
				final Clob observationClob = rowSet.getClob("obs_observation_blob");

				if (observationClob != null)
				{
					final BlobType blobType = new BlobType();
					blobType.getContent().add(JDBCUtil.getClobString(observationClob));
					observationFactType.setObservationBlob(blobType);
				}
			}

			if (obsFactStatusFlag)
			{
				if (rowSet.getTimestamp("obs_update_date") != null)
				{
					observationFactType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"obs_update_date").getTime()));
				}

				if (rowSet.getTimestamp("obs_download_date") != null)
				{
					observationFactType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"obs_download_date").getTime()));
				}

				if (rowSet.getTimestamp("obs_import_date") != null)
				{
					observationFactType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"obs_import_date").getTime()));
				}

				observationFactType.setSourcesystemCd(rowSet
						.getString("obs_sourcesystem_cd"));
			}

			return observationFactType;
		}
	}

	/**
	 * Inner class to build Patient dimension in Plain PDO format
	 */
	public class PatientBuilder
	{
		boolean patientDetailFlag = false;
		boolean patientBlobFlag = false;
		boolean patientStatusFlag = false;

		/**
		 * Patameter constructor
		 * 
		 * @param detailFlag
		 * @param blobFlag
		 * @param statusFlag
		 */
		public PatientBuilder(final boolean detailFlag, final boolean blobFlag,
				final boolean statusFlag)
		{
			this.patientDetailFlag = detailFlag;
			this.patientBlobFlag = blobFlag;
			this.patientStatusFlag = statusFlag;
		}

		/**
		 * Function reads single row from the resultset and builds patient dimension
		 * 
		 * @param rowSet
		 * @return Patient dimension type
		 * @throws SQLException
		 * @throws IOException
		 */
		public PatientType buildPatientSet(final ResultSet rowSet) throws SQLException,
				IOException
		{
			final PatientType patientDimensionType = new PatientType();
			final PatientIdType patientIdType = new PatientIdType();
			patientIdType.setValue(rowSet.getString("patient_patient_num"));
			patientDimensionType.setPatientId(patientIdType);

			final List<ParamType> paramTypeList = patientDimensionType.getParam();
			ParamType paramType = null;
			if (patientDetailFlag)
			{
				final String vitalStatusCd = rowSet.getString("patient_vital_status_cd");
				paramType = new ParamType();
				paramType.setName("vital_status_cd");
				paramType.setColumn("vital_status_cd");
				paramType.setValue(vitalStatusCd);
				paramTypeList.add(paramType);

				final Date birthDate = rowSet.getTimestamp("patient_birth_date");
				if (birthDate != null)
				{
					paramType = new ParamType();
					paramType.setName("birth_date");
					paramType.setColumn("birth_date");
					paramType.setValue(dtoFactory.getXMLGregorianCalendar(
							birthDate.getTime()).toString());
					paramTypeList.add(paramType);
				}
				final Date deathDate = rowSet.getTimestamp("patient_death_date");
				if (deathDate != null)
				{
					paramType = new ParamType();
					paramType.setName("death_date");
					paramType.setColumn("death_date");
					paramType.setValue(dtoFactory.getXMLGregorianCalendar(
							deathDate.getTime()).toString());
					paramTypeList.add(paramType);
				}
				paramType = new ParamType();
				paramType.setName("sex_cd");
				paramType.setColumn("sex_cd");
				// -----------------------------------------------------
				// $FURTHeR$ Oren Livne 5-OCT-2009
				// Change to a better display name in Dem1 plugin
				// $FURTHeR$ Oren Livne 6-OCT-2009
				// Added fall back for null values

				// paramType.setValue(rowSet.getString("patient_sex_cd"));
				setValue(paramType, rowSet, "sex_name");
				// -----------------------------------------------------
				paramTypeList.add(paramType);

				paramType = new ParamType();
				paramType.setName("age_in_years_num");
				paramType.setColumn("age_in_years_num");
				paramType.setValue(rowSet.getString("patient_age_in_years_num"));
				paramTypeList.add(paramType);

				paramType = new ParamType();
				paramType.setName("language_cd");
				paramType.setColumn("language_cd");
				// -----------------------------------------------------
				// $FURTHeR$ Oren Livne 6-OCT-2009
				// Change to a better display name in Dem1 plugin
				// paramType.setValue(rowSet.getString("patient_language_cd"));
				setValue(paramType, rowSet, "language_name");
				// -----------------------------------------------------
				paramTypeList.add(paramType);

				paramType = new ParamType();
				paramType.setName("race_cd");
				paramType.setColumn("race_cd");
				// -----------------------------------------------------
				// $FURTHeR$ Oren Livne 6-OCT-2009
				// Change to a better display name in Dem1 plugin
				// paramType.setValue(rowSet.getString("patient_race_cd"));
				setValue(paramType, rowSet, "race_name");
				// -----------------------------------------------------
				paramTypeList.add(paramType);

				paramType = new ParamType();
				paramType.setName("religion_cd");
				paramType.setColumn("religion_cd");
				// -----------------------------------------------------
				// $FURTHeR$ Oren Livne 6-OCT-2009
				// Change to a better display name in Dem1 plugin
				// paramType.setValue(rowSet.getString("patient_religion_cd"));
				setValue(paramType, rowSet, "religion_name");
				// -----------------------------------------------------
				paramTypeList.add(paramType);

				paramType = new ParamType();
				paramType.setName("marital_status_cd");
				paramType.setColumn("marital_status_cd");
				// -----------------------------------------------------
				// $FURTHeR$ Oren Livne 6-OCT-2009
				// Change to a better display name in Dem1 plugin
				// paramType.setValue(rowSet.getString("patient_marital_status_cd"));
				setValue(paramType, rowSet, "marital_status_name");
				// -----------------------------------------------------
				paramTypeList.add(paramType);

				paramType = new ParamType();
				paramType.setName("zipcode_char");
				paramType.setColumn("zipcode_char");
				paramType.setValue(rowSet.getString("patient_zip_cd"));
				paramTypeList.add(paramType);

				paramType = new ParamType();
				paramType.setName("statecityzip_path_char");
				paramType.setColumn("statecityzip_path_char");
				paramType.setValue(rowSet.getString("patient_statecityzip_path"));
				paramTypeList.add(paramType);

			}

			if (patientBlobFlag)
			{
				final Clob patientClob = rowSet.getClob("patient_patient_blob");

				if (patientClob != null)
				{
					final BlobType patientBlobType = new BlobType();
					patientBlobType.getContent().add(JDBCUtil.getClobString(patientClob));
					patientDimensionType.setPatientBlob(patientBlobType);
				}
			}

			if (patientStatusFlag)
			{
				if (rowSet.getTimestamp("patient_update_date") != null)
				{
					patientDimensionType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"patient_update_date").getTime()));
				}

				if (rowSet.getTimestamp("patient_download_date") != null)
				{
					patientDimensionType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"patient_download_date").getTime()));
				}

				if (rowSet.getTimestamp("patient_import_date") != null)
				{
					patientDimensionType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"patient_import_date").getTime()));
				}

				patientDimensionType.setSourcesystemCd(rowSet
						.getString("patient_sourcesystem_cd"));
			}

			return patientDimensionType;
		}
	}

	/**
	 * Inner class to build provider dimension in plain pdo format
	 */
	public class ProviderBuilder
	{
		boolean providerDetailFlag = false;
		boolean providerBlobFlag = false;
		boolean providerStatusFlag = false;

		/**
		 * Parameter constructor
		 * 
		 * @param detailFlag
		 * @param blobFlag
		 * @param statusFlag
		 */
		public ProviderBuilder(final boolean detailFlag, final boolean blobFlag,
				final boolean statusFlag)
		{
			this.providerDetailFlag = detailFlag;
			this.providerBlobFlag = blobFlag;
			this.providerStatusFlag = statusFlag;
		}

		/**
		 * Reads single row from resultset and builds provider dimension
		 * 
		 * @param rowSet
		 * @return
		 * @throws SQLException
		 * @throws IOException
		 */
		public ObserverType buildObserverSet(final ResultSet rowSet) throws SQLException,
				IOException
		{
			final ObserverType providerDimensionType = new ObserverType();
			providerDimensionType.setObserverCd(rowSet.getString("provider_provider_id"));
			providerDimensionType.setObserverPath(rowSet
					.getString("provider_provider_path"));

			if (providerDetailFlag)
			{
				providerDimensionType.setNameChar(rowSet.getString("provider_name_char"));
			}

			if (providerBlobFlag)
			{
				final Clob providerClob = rowSet.getClob("provider_provider_blob");

				if (providerClob != null)
				{
					final BlobType providerBlobType = new BlobType();
					providerBlobType.getContent().add(
							JDBCUtil.getClobString(providerClob));
					providerDimensionType.setObserverBlob(providerBlobType);
				}
			}

			if (providerStatusFlag)
			{
				if (rowSet.getTimestamp("provider_update_date") != null)
				{
					providerDimensionType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"provider_update_date").getTime()));
				}

				if (rowSet.getTimestamp("provider_download_date") != null)
				{
					providerDimensionType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"provider_download_date").getTime()));
				}

				if (rowSet.getTimestamp("provider_import_date") != null)
				{
					providerDimensionType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"provider_import_date").getTime()));
				}

				providerDimensionType.setSourcesystemCd(rowSet
						.getString("provider_sourcesystem_cd"));
			}

			return providerDimensionType;
		}
	}

	/**
	 * Inner class to build concept dimension in plain pdo format
	 */
	public class ConceptBuilder
	{
		boolean conceptDetailFlag = false;
		boolean conceptBlobFlag = false;
		boolean conceptStatusFlag = false;

		/**
		 * Parameter Constuctor
		 * 
		 * @param detailFlag
		 * @param blobFlag
		 * @param statusFlag
		 */
		public ConceptBuilder(final boolean detailFlag, final boolean blobFlag,
				final boolean statusFlag)
		{
			this.conceptDetailFlag = detailFlag;
			this.conceptBlobFlag = blobFlag;
			this.conceptStatusFlag = statusFlag;
		}

		/**
		 * Reads one row from result set and builds concept dimension
		 * 
		 * @param rowSet
		 * @return
		 * @throws SQLException
		 * @throws IOException
		 */
		public ConceptType buildConceptSet(final ResultSet rowSet) throws SQLException,
				IOException
		{
			final ConceptType conceptDimensionType = new ConceptType();

			conceptDimensionType.setConceptCd(rowSet.getString("concept_concept_cd"));

			if (conceptDetailFlag)
			{
				conceptDimensionType.setConceptCd(rowSet.getString("concept_concept_cd"));
				conceptDimensionType.setConceptPath(rowSet
						.getString("concept_concept_path"));
				conceptDimensionType.setNameChar(rowSet.getString("concept_name_char"));
			}

			if (conceptBlobFlag)
			{
				final BlobType conceptBlobType = new BlobType();
				conceptBlobType.getContent().add(rowSet.getBlob("concept_concept_blob"));
				conceptDimensionType.setConceptBlob(conceptBlobType);
			}

			if (conceptStatusFlag)
			{
				if (rowSet.getTimestamp("concept_update_date") != null)
				{
					conceptDimensionType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"concept_update_date").getTime()));
				}

				if (rowSet.getTimestamp("concept_download_date") != null)
				{
					conceptDimensionType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"concept_download_date").getTime()));
				}

				if (rowSet.getTimestamp("concept_import_date") != null)
				{
					conceptDimensionType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"concept_import_date").getTime()));
				}

				conceptDimensionType.setSourcesystemCd(rowSet
						.getString("concept_sourcesystem_cd"));
			}

			return conceptDimensionType;
		}
	}

	/**
	 * Inner class to build visit dimension in plain pdo format
	 */
	public class EventBuilder
	{
		boolean eventDetailFlag = false;
		boolean eventBlobFlag = false;
		boolean eventStatusFlag = false;

		public EventBuilder(final boolean detailFlag, final boolean blobFlag,
				final boolean statusFlag)
		{
			this.eventDetailFlag = detailFlag;
			this.eventBlobFlag = blobFlag;
			this.eventStatusFlag = statusFlag;
		}

		/**
		 * Reads one row from result set and builds visit/event dimension
		 * 
		 * @param rowSet
		 * @return
		 * @throws SQLException
		 * @throws IOException
		 */
		public EventType buildEventSet(final ResultSet rowSet) throws SQLException,
				IOException
		{
			final EventType visitDimensionType = new EventType();

			final PatientIdType patientIdType = new PatientIdType();
			patientIdType.setValue(rowSet.getString("visit_patient_num"));
			visitDimensionType.setPatientId(patientIdType);
			final EventType.EventId eventId = new EventType.EventId();
			eventId.setValue(rowSet.getString("visit_encounter_num"));
			visitDimensionType.setEventId(eventId);

			if (eventDetailFlag)
			{

				final ParamType inoutParamType = new ParamType();
				inoutParamType.setValue(rowSet.getString("visit_inout_cd"));
				inoutParamType.setColumn("inout_cd");
				visitDimensionType.getParam().add(inoutParamType);

				final ParamType locationParamType = new ParamType();
				locationParamType.setValue(rowSet.getString("visit_location_cd"));
				locationParamType.setColumn("location_cd");
				visitDimensionType.getParam().add(locationParamType);

				final ParamType siteParamType = new ParamType();
				locationParamType.setColumn("site_cd");
				siteParamType.setValue(rowSet.getString("visit_location_path"));
				visitDimensionType.getParam().add(siteParamType);

				final Date startDate = rowSet.getTimestamp("visit_start_date");

				if (startDate != null)
				{
					visitDimensionType.setStartDate(dtoFactory
							.getXMLGregorianCalendar(startDate.getTime()));
				}

				final Date endDate = rowSet.getTimestamp("visit_end_date");

				if (endDate != null)
				{
					visitDimensionType.setEndDate(dtoFactory
							.getXMLGregorianCalendar(endDate.getTime()));
				}
			}

			if (eventBlobFlag)
			{
				final Clob visitClob = rowSet.getClob("visit_visit_blob");

				if (visitClob != null)
				{
					final BlobType visitBlobType = new BlobType();
					visitBlobType.getContent().add(JDBCUtil.getClobString(visitClob));
					visitDimensionType.setEventBlob(visitBlobType);
				}
			}

			if (eventStatusFlag)
			{
				if (rowSet.getTimestamp("visit_update_date") != null)
				{
					visitDimensionType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"visit_update_date").getTime()));
				}

				if (rowSet.getTimestamp("visit_download_date") != null)
				{
					visitDimensionType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"visit_download_date").getTime()));
				}

				if (rowSet.getTimestamp("visit_import_date") != null)
				{
					visitDimensionType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"visit_import_date").getTime()));
				}

				visitDimensionType.setSourcesystemCd(rowSet
						.getString("visit_sourcesystem_cd"));
			}

			return visitDimensionType;
		}
	}

	// -----------------------------------------------------
	// $FURTHeR$ Oren Livne 6-OCT-2009

	/**
	 * Set a parameter type value to a null-safe value from a row set column.
	 */
	private void setValue(final ParamType paramType, final ResultSet rowSet,
			final String columnLabel) throws SQLException
	{
		paramType.setValue(getStringWithFallback(rowSet, columnLabel));
	}

	/**
	 * Return a result set row set column string value, or a fallback value if the column
	 * is null.
	 */
	private String getStringWithFallback(final ResultSet rowSet, final String columnLabel)
			throws SQLException
	{
		return getStringWithFallback(rowSet.getString(columnLabel));
	}

	/**
	 * Return a string value, or a fallback value if the column is null.
	 */
	private String getStringWithFallback(final String s)
	{
		return (s == null) ? "???" : s;
	}

	// -----------------------------------------------------

}
