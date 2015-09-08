/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE ASTBuilder plug-in.
 * 
 * The ULTIMATE ASTBuilder plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE ASTBuilder plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE ASTBuilder plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE ASTBuilder plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE ASTBuilder plug-in grant you additional permission 
 * to convey the resulting work.
 */
/* Grammar -- Automatically generated by TreeBuilder */

package de.uni_freiburg.informatik.ultimate.astbuilder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a grammar.
 */
public class Grammar {
    /**
     * The package name of this grammar.
     */
    String packageName;

    /**
     * The imports of this grammar.
     */
    ArrayList<String> imports;

    /**
     * The node table of this grammar.
     */
    HashMap<String,Node> nodeTable;

    /**
     * The constructor taking initial values.
     * @param packageName the package name of this grammar.
     * @param imports the imports of this grammar.
     * @param nodeTable the node table of this grammar.
     */
    public Grammar(String packageName, ArrayList<String> imports, HashMap<String,Node> nodeTable) {
        super();
        this.packageName = packageName;
        this.imports = imports;
        this.nodeTable = nodeTable;
    }

    /**
     * Returns a textual description of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Grammar").append('[');
        sb.append(packageName);
        sb.append(',').append(imports);
        sb.append(',').append(nodeTable);
        return sb.append(']').toString();
    }

    /**
     * Gets the package name of this grammar.
     * @return the package name of this grammar.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Gets the imports of this grammar.
     * @return the imports of this grammar.
     */
    public ArrayList<String> getImports() {
        return imports;
    }

    /**
     * Gets the node table of this grammar.
     * @return the node table of this grammar.
     */
    public HashMap<String,Node> getNodeTable() {
        return nodeTable;
    }
}
