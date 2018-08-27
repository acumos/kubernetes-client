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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation of Operation Signature list of a Node. IMPORTANT: This itself
 * is NOT an Arraylist.
 */

public class InputField implements Serializable {
	
	private static final long serialVersionUID = -4128972336728930429L;

	@JsonProperty("mapped_to_field")
	private String mappedToField = null;

	@JsonProperty("name")
    private String name  = null;

	@JsonProperty("type")
    private String type = null;

	@JsonProperty("checked")
    private String checked = null;

   

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getType ()
    {
        return type;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    public String getChecked ()
    {
        return checked;
    }

    public String getMappedToField() {
		return mappedToField;
	}

	public void setMappedToField(String mappedToField) {
		this.mappedToField = mappedToField;
	}

	public void setChecked (String checked)
    {
        this.checked = checked;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [mapped_to_field = "+mappedToField+", name = "+name+", type = "+type+", checked = "+checked+"]";
    }	

	/**
	 * Standard POJO no-arg constructor
	 */
	public InputField() {
		super();
	}	
}