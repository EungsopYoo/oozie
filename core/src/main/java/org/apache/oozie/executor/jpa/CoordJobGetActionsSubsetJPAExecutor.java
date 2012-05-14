/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.oozie.executor.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.oozie.CoordinatorActionBean;
import org.apache.oozie.ErrorCode;
import org.apache.oozie.util.ParamChecker;

/**
 * Load coordinator actions by start and len (a subset) for a coordinator job.
 */
public class CoordJobGetActionsSubsetJPAExecutor implements JPAExecutor<List<CoordinatorActionBean>> {

    private String coordJobId = null;
    private int start = 1;
    private int len = 50;
    private List<String> filterList;

    public CoordJobGetActionsSubsetJPAExecutor(String coordJobId) {
        ParamChecker.notNull(coordJobId, "coordJobId");
        this.coordJobId = coordJobId;
    }

    public CoordJobGetActionsSubsetJPAExecutor(String coordJobId, List<String> filterList, int start, int len) {
        this(coordJobId);
        ParamChecker.notNull(filterList, "filterList");
        this.filterList = filterList;
        this.start = start;
        this.len = len;
    }

    @Override
    public String getName() {
        return "CoordJobGetActionsSubsetJPAExecutor";
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CoordinatorActionBean> execute(EntityManager em) throws JPAExecutorException {
        List<CoordinatorActionBean> actions;
        List<CoordinatorActionBean> actionList = new ArrayList<CoordinatorActionBean>();
        try {
            Query q = em.createNamedQuery("GET_ACTIONS_FOR_COORD_JOB_ORDER_BY_NOMINAL_TIME");
            if (!filterList.isEmpty()) {
                // Add the filter clause
                String query = q.toString();
                StringBuilder sbTotal = new StringBuilder(query);
                int offset = query.lastIndexOf("order");
                // Get the 'where' clause for status filters
                StringBuilder statusClause = getStatusClause(filterList);
                // Insert 'where' before 'order by'
                sbTotal.insert(offset, statusClause);
                q = em.createQuery(sbTotal.toString());
            }
            q.setParameter("jobId", coordJobId);
            q.setFirstResult(start - 1);
            q.setMaxResults(len);
            actions = q.getResultList();

            for (CoordinatorActionBean a : actions) {
                CoordinatorActionBean aa = getBeanForRunningCoordAction(a);
                actionList.add(aa);
            }
        }
        catch (Exception e) {
            throw new JPAExecutorException(ErrorCode.E0603, e);
        }
        return actionList;
    }

    // Form the where clause to filter by status values
    private StringBuilder getStatusClause(List<String> filterList) {
        StringBuilder sb = new StringBuilder();
        boolean isStatus = false;
        for (String statusVal : filterList) {
            if (!isStatus) {
                sb.append(" and a.status IN (\'" + statusVal + "\'");
                isStatus = true;
            }
            else {
                sb.append(",\'" + statusVal + "\'");
            }
        }
        sb.append(") ");
        return sb;
    }

    private CoordinatorActionBean getBeanForRunningCoordAction(CoordinatorActionBean a) {
        if (a != null) {
            CoordinatorActionBean action = new CoordinatorActionBean();
            action.setId(a.getId());
            action.setActionNumber(a.getActionNumber());
            action.setActionXml(a.getActionXml());
            action.setConsoleUrl(a.getConsoleUrl());
            action.setCreatedConf(a.getCreatedConf());
            action.setExternalStatus(a.getExternalStatus());
            action.setMissingDependencies(a.getMissingDependencies());
            action.setRunConf(a.getRunConf());
            action.setTimeOut(a.getTimeOut());
            action.setTrackerUri(a.getTrackerUri());
            action.setType(a.getType());
            action.setCreatedTime(a.getCreatedTime());
            action.setExternalId(a.getExternalId());
            action.setJobId(a.getJobId());
            action.setLastModifiedTime(a.getLastModifiedTime());
            action.setNominalTime(a.getNominalTime());
            action.setSlaXml(a.getSlaXml());
            action.setStatus(a.getStatus());
            return action;
        }
        return null;
    }

}
