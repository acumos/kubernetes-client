/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * ===================================================================================
 * This Acumos software file is distributed by AT&T and Tech Mahindra
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END=========================================================
 */
package com.dockerKube.parsebean;

import java.io.Serializable;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation of Operation Signature list of a Node. IMPORTANT: This itself
 * is NOT an Arraylist.
 */

public class OutputField implements Serializable {
	
	private static final long serialVersionUID = -606679948054329684L;

	@JsonProperty("tag")
	private String tag = null;

	@JsonProperty("name")
	private String name = null;

	@JsonProperty("type_and_role_hierarchy_list")
	private ArrayList<TypeAndRoleHierarchyList>  typeAndRoleHierarchyList;
	
	public OutputField(String tag, String name, ArrayList<TypeAndRoleHierarchyList>  typeAndRoleHierarchyList) {
		super();
		this.tag = tag;
		this.name = name;
		this.typeAndRoleHierarchyList = typeAndRoleHierarchyList;
	}
	public OutputField(){
		super();
	}

	public String getTag() {
		return tag;
	}	

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}	
	
	public ArrayList<TypeAndRoleHierarchyList> getTypeAndRoleHierarchyList() {
		return typeAndRoleHierarchyList;
	}

	public void setTypeAndRoleHierarchyList(ArrayList<TypeAndRoleHierarchyList> typeAndRoleHierarchyList) {
		this.typeAndRoleHierarchyList = typeAndRoleHierarchyList;
	}

	@Override
    public String toString()
    {
        return "ClassPojo [tag = "+tag+", name = "+name+", type_and_role_hierarchy_list = "+typeAndRoleHierarchyList+"]";
    }	
}