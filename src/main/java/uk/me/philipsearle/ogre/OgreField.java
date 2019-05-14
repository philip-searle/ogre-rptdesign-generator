package uk.me.philipsearle.ogre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.dom4j.Element;

class OgreField implements TreeNode {
	private final Element sampleXmlElement;
	private final String name;
	private final OgreField parent;
	private final List<OgreField> children = new ArrayList<>();
	private boolean multiValued;

	public OgreField(Element sampleXmlElement) {
		this.sampleXmlElement = sampleXmlElement;
		this.name = "<root>";
		this.parent = this;
		this.multiValued = true;
	}

	OgreField(Element sampleXmlElement, String name, OgreField parent) {
		this.sampleXmlElement = sampleXmlElement;
		this.name = name;
		this.parent = parent;
		parent.children.add(this);
	}
	
	void setMultiValued(boolean multiValued) {
		this.multiValued = multiValued;
	}
	
	@Override
	public String toString() {
		return name;
	}

	String getUniqueId() {
		if (this == parent) {
			return "";
		}
		return parent.getUniqueId() + ":" + this.name;
	}

	public Iterable<OgreField> childFields() {
		return children;
	}

	public String getName() {
		return name;
	}

	public String getValueXPath() {
		return sampleXmlElement.getPath();
	}

	public String getValueXPathRelativeTo(OgreField ancestor) {
		return getValueXPath().substring(ancestor.getValueXPath().length());
	}

	public boolean isMultiValued() {
		return multiValued;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration children() {
		return Collections.enumeration(children);
	}

	@Override
	public boolean getAllowsChildren() {
		return !children.isEmpty();
	}

	@Override
	public TreeNode getChildAt(int index) {
		return children.get(index);
	}

	@Override
	public int getChildCount() {
		return children.size();
	}

	@Override
	public int getIndex(TreeNode child) {
		return children.indexOf(child);
	}

	@Override
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public boolean isLeaf() {
		return children.isEmpty();
	}

	public List<OgreField> flatten() {
		List<OgreField> flatList = new ArrayList<>();
		for (OgreField childField : children) {
			flatList.add(childField);
			if (!childField.isLeaf()) {
				flatList.addAll(childField.flatten());
			}
		}
		return flatList;
	}
}