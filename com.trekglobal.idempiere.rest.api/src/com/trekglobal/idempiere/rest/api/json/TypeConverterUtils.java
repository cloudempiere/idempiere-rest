/**********************************************************************
* This file is part of iDempiere ERP Open Source                      *
* http://www.idempiere.org                                            *
*                                                                     *
* Copyright (C) Contributors                                          *
*                                                                     *
* This program is free software; you can redistribute it and/or       *
* modify it under the terms of the GNU General Public License         *
* as published by the Free Software Foundation; either version 2      *
* of the License, or (at your option) any later version.              *
*                                                                     *
* This program is distributed in the hope that it will be useful,     *
* but WITHOUT ANY WARRANTY; without even the implied warranty of      *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
* GNU General Public License for more details.                        *
*                                                                     *
* You should have received a copy of the GNU General Public License   *
* along with this program; if not, write to the Free Software         *
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
* MA 02110-1301, USA.                                                 *
*                                                                     *
* Contributors:                                                       *
* - Trek Global Corporation                                           *
* - Heng Sin Low                                                      *
**********************************************************************/
package com.trekglobal.idempiere.rest.api.json;

import static org.compiere.util.DisplayType.Account;
import static org.compiere.util.DisplayType.Binary;
import static org.compiere.util.DisplayType.Button;
import static org.compiere.util.DisplayType.ID;
import static org.compiere.util.DisplayType.Image;
import static org.compiere.util.DisplayType.Location;
import static org.compiere.util.DisplayType.Locator;
import static org.compiere.util.DisplayType.PAttribute;
import static org.compiere.util.DisplayType.Payment;
import static org.compiere.util.DisplayType.RecordID;
import static org.compiere.util.DisplayType.JSON;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import org.adempiere.base.Service;
import org.adempiere.base.ServiceQuery;
import org.compiere.model.GridField;
import org.compiere.model.Lookup;
import org.compiere.model.MColumn;
import org.compiere.util.DisplayType;
import org.compiere.util.NamePair;
import org.compiere.util.Util;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.trekglobal.idempiere.rest.api.model.MRestView;

/**
 * @author hengsin
 *
 */
public class TypeConverterUtils {

	private static final Pattern NONLATIN = Pattern.compile("[^\\w_-]");  
	private static final Pattern SEPARATORS = Pattern.compile("[\\s\\p{Punct}&&[^-]&&[^_]]");
	
	/**
	 * private constructor
	 */
	private TypeConverterUtils() {
	}

	/**
	 * Convert table's column name to json property name
	 * @param columnName
	 * @return propertyName
	 */
	public static String toPropertyName(String columnName) {
		if (Util.isEmpty(columnName))
			return columnName;

		String propertyName = columnName;
		if (!propertyName.contains("_")) {
			String initial = propertyName.substring(0, 1).toLowerCase();
			propertyName = initial + propertyName.substring(1);
		}
		return propertyName;
	}
	
	/**
	 * Convert db column value to json value
	 * @param column
	 * @param value
	 * @return Object
	 */
	public static Object toJsonValue(MColumn column, Object value) {
		return toJsonValue(column, value, null, null);
	}

	/**
	 * Convert db column value to json value
	 * @param column
	 * @param value
	 * @param referenceView
	 * @return Object
	 */
	public static Object toJsonValue(MColumn column, Object value, MRestView referenceView) {
		return toJsonValue(column, value, referenceView, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	/**
	 * Convert db column value to json value within the given transaction
	 * @param column
	 * @param value
	 * @param referenceView
	 * @param trxName transaction name, or null to read committed data
	 * @return Object
	 */
	public static Object toJsonValue(MColumn column, Object value, MRestView referenceView, String trxName) {
		ITypeConverter typeConverter = getTypeConverter(column.getAD_Reference_ID(), value);

		if (typeConverter != null) {
			return typeConverter.toJsonValue(column, value, referenceView, trxName);
		} else if (value != null && DisplayType.isText(column.getAD_Reference_ID())) {
			return value.toString();
		} else if (value != null && column.getAD_Reference_ID() == DisplayType.ID && value instanceof Number) {
			return ((Number)value).intValue();
		} else {
			return null;
		}
	}

	/**
	 * Convert db column value to json value
	 * @param field
	 * @param value
	 * @return Object
	 */
	public static Object toJsonValue(GridField field, Object value) {
		return toJsonValue(field, value, (String)null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	/**
	 * Convert db column value to json value within the given transaction
	 * @param field
	 * @param value
	 * @param trxName transaction name, or null to read committed data
	 * @return Object
	 */
	public static Object toJsonValue(GridField field, Object value, String trxName) {
		ITypeConverter typeConverter = getTypeConverter(field.getDisplayType(), value);

		if (typeConverter != null) {
			return typeConverter.toJsonValue(field, value, trxName);
		} else if (value != null && DisplayType.isText(field.getDisplayType())) {
			return value.toString();
		} else if (value != null && field.getDisplayType() == DisplayType.ID && value instanceof Number) {
			return ((Number)value).intValue();
		} else {
			return null;
		}
	}

	/**
	 * Convert json value to db column value
	 * @param column
	 * @param value
	 * @return Object
	 */
	public static Object fromJsonValue(MColumn column, JsonElement value) {
		return fromJsonValue(column, value, null, null);
	}

	/**
	 * Convert json value to db column value
	 * @param column
	 * @param value
	 * @param referenceView
	 * @return Object
	 */
	public static Object fromJsonValue(MColumn column, JsonElement value, MRestView referenceView) {
		return fromJsonValue(column, value, referenceView, null);
	}

	@SuppressWarnings("rawtypes")
	/**
	 * Convert json value to db column value within the given transaction
	 * @param column
	 * @param value
	 * @param referenceView
	 * @param trxName transaction name, or null for auto-commit
	 * @return Object
	 */
	public static Object fromJsonValue(MColumn column, JsonElement value, MRestView referenceView, String trxName) {
		ITypeConverter typeConverter = getTypeConverter(column.getAD_Reference_ID(), value);

		if (typeConverter != null) {
			return typeConverter.fromJsonValue(column, value, referenceView, trxName);
		} else if (value != null && !(value instanceof JsonNull) && DisplayType.isText(column.getAD_Reference_ID())) {
			return value.getAsString();
		} else {
			return null;
		}
	}

	/**
	 * Convert json value to db column value
	 * @param gridField
	 * @param value
	 * @return Object
	 */
	public static Object fromJsonValue(GridField gridField, JsonElement value) {
		return fromJsonValue(gridField, value, (String)null);
	}

	@SuppressWarnings("rawtypes")
	/**
	 * Convert json value to db column value within the given transaction
	 * @param gridField
	 * @param value
	 * @param trxName transaction name, or null for auto-commit
	 * @return Object
	 */
	public static Object fromJsonValue(GridField gridField, JsonElement value, String trxName) {
		ITypeConverter typeConverter = getTypeConverter(gridField.getDisplayType(), value);

		if (typeConverter != null) {
			return typeConverter.fromJsonValue(gridField, value, trxName);
		} else if (value != null && !(value instanceof JsonNull) && DisplayType.isText(gridField.getDisplayType())) {
			return value.getAsString();
		} else {
			return null;
		}
	}
	
	/**
	 * Resolve the display identifier for a lookup value within the given transaction.
	 * <p>Uses {@link Lookup#getDirect(Object, boolean, boolean, String)} so a referenced row
	 * co-created earlier in the same still-open request transaction is visible, and falls back
	 * to the cached {@link Lookup#getDisplay(Object)} when no transaction is supplied or the
	 * direct read returns nothing.
	 * @param lookup lookup
	 * @param value key value
	 * @param trxName transaction name, or null to read committed data
	 * @return display identifier
	 */
	public static String getIdentifier(Lookup lookup, Object value, String trxName) {
		if (trxName != null) {
			NamePair pair = lookup.getDirect(value, false, false, trxName);
			if (pair != null)
				return pair.getName();
		}
		return lookup.getDisplay(value);
	}

	/**
	 * convert arbitrary text to slug
	 * @param input
	 * @return slug
	 */
	public static String slugify(String input) {
		String noseparators = SEPARATORS.matcher(input).replaceAll("-");
	    String normalized = Normalizer.normalize(noseparators, Form.NFD);
	    String slug = NONLATIN.matcher(normalized).replaceAll("");
	    return slug.toLowerCase(Locale.ENGLISH).replaceAll("-{2,}","-").replaceAll("^-|-$","");
	}
	
	@SuppressWarnings({ "rawtypes" })
	private static ITypeConverter getTypeConverter(int displayType, Object value) {
		ITypeConverter typeConverter = null;
		ServiceQuery query = new ServiceQuery();
		query.put("displayType", Integer.toString(displayType));
		typeConverter = Service.locator().locate(ITypeConverter.class, query).getService();
		if (typeConverter == null) {
			if (((DisplayType.isNumeric(displayType) || displayType == Button || displayType == RecordID || displayType == ID) && value instanceof Number)) {
				typeConverter = new NumericTypeConverter();
			} else if (DisplayType.isDate(displayType) && value instanceof Date) {
				typeConverter = new DateTypeConverter();
			} else if (DisplayType.YesNo == displayType) {
				typeConverter = new YesNoTypeConverter();
			}else if(displayType==Location){
				return new LocationTypeConverter();
			} else if (displayType == Locator
					|| displayType == Account
					|| displayType == PAttribute
					|| displayType == Payment
					|| DisplayType.isLookup(displayType)) {
				return new LookupTypeConverter();
			} else if (displayType == Binary) {
				return new BinaryTypeConverter();
			} else if (displayType == Image) {
				return new ImageTypeConverter();		
			} else if (displayType == JSON) {
				return new JSONTypeConverter();		
			}
		}
		return typeConverter;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private static ITypeConverter getTypeConverter(int displayType, JsonElement value) {
		ITypeConverter typeConverter = null;
		ServiceQuery query = new ServiceQuery();
		query.put("displayType", Integer.toString(displayType));
		typeConverter = Service.locator().locate(ITypeConverter.class, query).getService();
		if (typeConverter == null) {
			if ((DisplayType.isNumeric(displayType) || displayType == Button || displayType == RecordID || displayType == ID) && (isNumber(value) || isString(value))) {
				typeConverter = new NumericTypeConverter();
			} else if (DisplayType.isDate(displayType) && isString(value)) {
				typeConverter = new DateTypeConverter();
			} else if (DisplayType.YesNo == displayType && (isBoolean(value) || isString(value))) {
				typeConverter = new YesNoTypeConverter();
			}else if(displayType==Location){
				return new LocationTypeConverter();
			}else if (displayType == Locator
					|| displayType == Account
					|| displayType == PAttribute
					|| displayType == Payment
					|| DisplayType.isLookup(displayType)) {
				return new LookupTypeConverter();
			} else if (displayType == Binary) {
				return new BinaryTypeConverter();
			}
			else if (displayType == Image) {
				return new ImageTypeConverter();
			} else if (displayType == JSON) {
				return new JSONTypeConverter();		
			}
		}
		return typeConverter;
	}

	private static boolean isBoolean(JsonElement value) {
		if (value instanceof JsonPrimitive) {
			JsonPrimitive primitive = (JsonPrimitive) value;
			return primitive.isBoolean();
		}
		return false;
	}

	private static boolean isString(JsonElement value) {
		if (value instanceof JsonPrimitive) {
			JsonPrimitive primitive = (JsonPrimitive) value;
			return primitive.isString();
		}
		return false;
	}

	private static boolean isNumber(JsonElement value) {
		if (value instanceof JsonPrimitive) {
			JsonPrimitive primitive = (JsonPrimitive) value;
			return primitive.isNumber();
		}
		return false;
	}		  	

}
