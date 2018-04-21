/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2012 Victor Kirhenshtein
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.netxms.client.log;

import java.util.HashSet;
import java.util.Set;

import org.netxms.base.NXCPMessage;
import org.netxms.client.constants.ColumnFilterSetOperation;
import org.netxms.client.constants.ColumnFilterType;

/**
 * Column filter
 */
public class ColumnFilter {
	private ColumnFilterType type;
	private long rangeFrom;
	private long rangeTo;
	private long numericValue;
	private String like;
	private HashSet<ColumnFilter> set;
	private ColumnFilterSetOperation operation; // Set operation: AND or OR
	private boolean negated = false;

	/**
	 * Create filter of type EQUALS, LESS, GREATER, or CHILDOF
	 * 
	 * @param value
	 */
	public ColumnFilter(ColumnFilterType type, long value) {
		this.type = type;
		numericValue = value;
	}

	/**
	 * Create filter of type RANGE
	 * 
	 * @param rangeFrom
	 * @param rangeTo
	 */
	public ColumnFilter(long rangeFrom, long rangeTo) {
		type = ColumnFilterType.RANGE;
		this.rangeFrom = rangeFrom;
		this.rangeTo = rangeTo;
	}

	/**
	 * Create filter of type LIKE
	 * 
	 * @param value
	 */
	public ColumnFilter(String value) {
		type = ColumnFilterType.LIKE;
		like = value;
	}

	/**
	 * Create filter of type SET
	 */
	public ColumnFilter() {
		type = ColumnFilterType.SET;
		set = new HashSet<ColumnFilter>();
		operation = ColumnFilterSetOperation.AND;
	}

	/**
	 * Add new element to SET type filter
	 * 
	 * @param filter
	 */
	public void addSubFilter(ColumnFilter filter) {
		if (type == ColumnFilterType.SET)
			set.add(filter);
	}

	/**
	 * Fill NXCP message with filters' data
	 * 
	 * @param msg
	 *            NXCP message
	 * @param baseId
	 *            Base variable ID
	 * @return Number of variables used
	 */
	int fillMessage(final NXCPMessage msg, final long baseId) {
		int varCount = 1;
		msg.setFieldInt16(baseId, type.getValue());
		switch (type) {
		case EQUALS:
		case LESS:
		case GREATER:
		case CHILDOF:
			msg.setFieldInt64(baseId + 1, numericValue);
			msg.setFieldInt16(baseId + 2, negated ? 1 : 0);
			varCount += 2;
			break;
		case RANGE:
			msg.setFieldInt64(baseId + 1, rangeFrom);
			msg.setFieldInt64(baseId + 2, rangeTo);
			msg.setFieldInt16(baseId + 3, negated ? 1 : 0);
			varCount += 3;
			break;
		case LIKE:
			msg.setField(baseId + 1, like);
			msg.setFieldInt16(baseId + 2, negated ? 1 : 0);
			varCount += 2;
			break;
		case SET:
			msg.setFieldInt16(baseId + 1, operation.getValue());
			msg.setFieldInt16(baseId + 2, set.size());
			varCount += 2;
			long varId = baseId + 3;
			for (final ColumnFilter f : set) {
				int count = f.fillMessage(msg, varId);
				varId += count;
				varCount += count;
			}
			break;
		default:
			break;
		}
		return varCount;
	}

	/**
	 * @return the rangeFrom
	 */
	public long getRangeFrom() {
		return rangeFrom;
	}

	/**
	 * @param rangeFrom
	 *            the rangeFrom to set
	 */
	public void setRangeFrom(long rangeFrom) {
		this.rangeFrom = rangeFrom;
	}

	/**
	 * @return the rangeTo
	 */
	public long getRangeTo() {
		return rangeTo;
	}

	/**
	 * @param rangeTo
	 *            the rangeTo to set
	 */
	public void setRangeTo(long rangeTo) {
		this.rangeTo = rangeTo;
	}

	/**
	 * @return the equalsTo
	 */
	public long getNumericValue() {
		return numericValue;
	}

	/**
	 * @param numericValue
	 *            numeric value to set
	 */
	public void setNumericValue(long numericValue) {
		this.numericValue = numericValue;
	}

	/**
	 * @return the like
	 */
	public String getLike() {
		return like;
	}

	/**
	 * @param like
	 *            the like to set
	 */
	public void setLike(String like) {
		this.like = like;
	}

	/**
	 * @return the operation
	 */
	public ColumnFilterSetOperation getOperation() {
		return operation;
	}

	/**
	 * @param operation
	 *            the operation to set
	 */
	public void setOperation(ColumnFilterSetOperation operation) {
		this.operation = operation;
	}

	/**
	 * @return the type
	 */
	public ColumnFilterType getType() {
		return type;
	}

	/**
	 * Get sub-filters.
	 * 
	 * @return Set of sub-filters
	 */
	public Set<ColumnFilter> getSubFilters() {
		return set;
	}

	/**
	 * @return the negated
	 */
	public boolean isNegated() {
		return negated;
	}

	/**
	 * @param negated
	 *            the negated to set
	 */
	public void setNegated(boolean negated) {
		this.negated = negated;
	}
}
