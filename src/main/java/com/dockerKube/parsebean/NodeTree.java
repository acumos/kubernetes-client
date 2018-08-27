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
import java.util.ArrayList;
import java.util.List;

public class NodeTree<T> {
 
	private T data = null;
 
	private List<NodeTree<T>> children = new ArrayList<>();
 
	private NodeTree<T> parent = null;
 
	public NodeTree(T data) {
		this.data = data;
	}
 
	public NodeTree<T> addChild(NodeTree<T> child) {
		child.setParent(this);
		this.children.add(child);
		return child;
	}
 
	public void addChildren(List<NodeTree<T>> children) {
		children.forEach(each -> each.setParent(this));
		this.children.addAll(children);
	}
 
	public List<NodeTree<T>> getChildren() {
		return children;
	}
 
	public T getData() {
		return data;
	}
 
	public void setData(T data) {
		this.data = data;
	}
 
	private void setParent(NodeTree<T> parent) {
		this.parent = parent;
	}
 
	public NodeTree<T> getParent() {
		return parent;
	}
 
}