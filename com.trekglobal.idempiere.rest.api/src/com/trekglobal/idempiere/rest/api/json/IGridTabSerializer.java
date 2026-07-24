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

import java.util.List;

import org.adempiere.base.Service;
import org.compiere.model.GridTab;

import com.google.gson.JsonObject;

/**
 * Serialize/Deserialize interface for GridTab
 * @author hengsin
 *
 */
public interface IGridTabSerializer {
	/**
	 * Convert current row to json
	 * @param gridTab
	 * @return JsonObject
	 */
	public default JsonObject toJson(GridTab gridTab) {
		return toJson(gridTab, (String[])null, (String[])null, (String)null);
	}

	/**
	 * Convert current row to json within the given transaction
	 * @param gridTab
	 * @param trxName transaction name, or null to read committed data
	 * @return JsonObject
	 */
	public default JsonObject toJson(GridTab gridTab, String trxName) {
		return toJson(gridTab, (String[])null, (String[])null, trxName);
	}

	/**
	 * Convert current row to json
	 * @param gridTab
	 * @param includes columns to include
	 * @param excludes columns to exclude
	 * @return JsonObject
	 */
	public JsonObject toJson(GridTab gridTab, String[] includes, String[] excludes);

	/**
	 * Convert current row to json within the given transaction
	 * @param gridTab
	 * @param includes columns to include
	 * @param excludes columns to exclude
	 * @param trxName transaction name, or null to read committed data
	 * @return JsonObject
	 */
	public default JsonObject toJson(GridTab gridTab, String[] includes, String[] excludes, String trxName) {
		return toJson(gridTab, includes, excludes);
	}

	/**
	 * Copy values from JsonObject to GridTab
	 * @param json
	 * @param gridTab
	 */
	public void fromJson(JsonObject json, GridTab gridTab);

	/**
	 * Copy values from JsonObject to GridTab within the given transaction
	 * @param json
	 * @param gridTab
	 * @param trxName transaction name, or null for auto-commit
	 */
	public default void fromJson(JsonObject json, GridTab gridTab, String trxName) {
		fromJson(json, gridTab);
	}
	
	/**
	 * Get GridTab serializer
	 * @param gridTabUID uuid of ad_tab
	 * @return IGridTabSerializer
	 */
	public static IGridTabSerializer getGridTabSerializer(String gridTabUID) {
		IGridTabSerializer serializer = null;
		List<IGridTabSerializerFactory> factories = Service.locator().list(IGridTabSerializerFactory.class).getServices();
		for (IGridTabSerializerFactory  factory : factories) {
			serializer = factory.getGridTabSerializer(gridTabUID);
			if (serializer != null) {
				break;
			}
		}
		if (serializer == null) {
			for (IGridTabSerializerFactory  factory : factories) {
				serializer = factory.getGridTabSerializer("*");
				if (serializer != null) {
					break;
				}
			}
		}
			
		
		return serializer;
	}
}
