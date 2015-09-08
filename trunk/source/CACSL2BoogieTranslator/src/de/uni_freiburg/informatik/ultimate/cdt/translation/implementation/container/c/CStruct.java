/*
 * Copyright (C) 2014-2015 Alexander Nutz (nutz@informatik.uni-freiburg.de)
 * Copyright (C) 2013-2015 Christian Schilling (schillic@informatik.uni-freiburg.de)
 * Copyright (C) 2012-2015 Markus Lindenmann (lindenmm@informatik.uni-freiburg.de)
 * Copyright (C) 2013-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE CACSL2BoogieTranslator plug-in.
 * 
 * The ULTIMATE CACSL2BoogieTranslator plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE CACSL2BoogieTranslator plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE CACSL2BoogieTranslator plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE CACSL2BoogieTranslator plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE CACSL2BoogieTranslator plug-in grant you additional permission 
 * to convey the resulting work.
 */
/**
 * Describes a struct given in C.
 */
package de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c;

import java.util.Arrays;

import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CPrimitive.PRIMITIVE;
import de.uni_freiburg.informatik.ultimate.util.HashUtils;

/**
 * @author Markus Lindenmann
 * @date 18.09.2012
 */
public class CStruct extends CType {
    /**
     * Field names.
     */
    private String[] fNames;
    /**
     * Field types.
     */
    private  CType[] fTypes;
    
    /**
     * Indicates if this represents an incomplete type.
     * If 'this' is complete, this String is empty,
     * otherwise it holds the name of the incomplete struct.
     */
//    private boolean isIncomplete;
    private String incompleteName = "";
    
    private String toStringCached = null;

    //@Override
    public boolean isIncomplete() {
//		return isIncomplete;
    	return !incompleteName.isEmpty();
//    	return incompleteName.equals("");
	}
    
//    public String getIncompleteName() {
//    	return incompleteName;
//    }

	/**
     * Constructor.
     * 
     * @param fNames
     *            field names.
     * @param fTypes
     *            field types.
     * @param cDeclSpec
     *            the C declaration used.
     */
    public CStruct(String[] fNames,
            CType[] fTypes) {
        super(false, false, false, false); //FIXME: integrate those flags
        this.fNames = fNames;
        this.fTypes = fTypes;
//        this.isIncomplete = false;
        this.incompleteName = "";
    }
    
    public CStruct(String name) { //boolean isIncomplete) {
        super(false, false, false, false); //FIXME: integrate those flags
//        if (!isIncomplete) {
//        	throw new AssertionError("use different constructor for non-incomplete types");
//        }
        this.fNames = new String[0];
        this.fTypes = new CType[0];
//        this.isIncomplete = isIncomplete;
        this.incompleteName = name;
    }

    /**
     * Get the number of fields in this struct.
     * 
     * @return the number of fields.
     */
    public int getFieldCount() {
        return fNames.length;
    }

    /**
     * Returns the field type, i.e. the type of the field at the given index.
     * 
     * @param id
     *            the fields id.
     * @return the field type.
     */
    public CType getFieldType(String id) {
    	assert !this.isIncomplete() : "Cannot get a field type in an incomplete struct type.";
        int idx = Arrays.asList(fNames).indexOf(id);
        if (idx < 0) {
            throw new IllegalArgumentException("Field '" + id
                    + "' not in struct!");
        }
        return fTypes[idx];
    }

    /**
     * Getter for all field types, orderd according to occurance in C code!
     * 
     * @return the types of this strut's fields.
     */
    public CType[] getFieldTypes() {
        return fTypes;
    }

    /**
     * Returns the set of fields in this struct.
     * 
     * @return the set of fields in this struct.
     */
    public String[] getFieldIds() {
        return fNames.clone();
    }

    @Override
    public String toString() {
//    	if (isIncomplete) {
    	if (this.isIncomplete()) {
    		return "STRUCT#~incomplete~" + incompleteName;
    	} else if (toStringCached != null) { 
    		return toStringCached;
    	}else {
    		StringBuilder id = new StringBuilder("STRUCT#");
    		for (int i = 0; i < getFieldCount(); i++) {
    			id.append("?");
    			id.append(fNames[i]);
    			id.append("~");
    			id.append(fTypes[i].toString());
    		}
    		id.append("#");
    		toStringCached = id.toString();
    		return toStringCached;
    	}
    }
    
    @Override
    public boolean equals(Object o) {
    	if (super.equals(o)) //to break a mutual recursion with CPointer -- TODO: is that a general solution??
    		return true;
        if (!(o instanceof CType)) {
            return false;
        }
        CType oType = ((CType)o).getUnderlyingType();
        if (!(oType instanceof CStruct)) {
            return false;
        }
        
        CStruct oStruct = (CStruct)oType;
        if (fNames.length != oStruct.fNames.length) {
            return false;
        }
        for (int i = fNames.length - 1; i >= 0; --i) {
            if (!(fNames[i].equals(oStruct.fNames[i]))) {
                return false;
            }
        }
        if (fTypes.length != oStruct.fTypes.length) {
            return false;
        }
        for (int i = fTypes.length - 1; i >= 0; --i) {
            if (!(fTypes[i].equals(oStruct.fTypes[i]))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 
     * @param cvar
     */
	public void complete(CStruct cvar) {
		if (!isIncomplete()) {
			throw new AssertionError("only incomplete structs can be completed");
		}
//		isIncomplete = false;
		incompleteName = "";
		fNames = cvar.fNames;
		fTypes = cvar.fTypes;
	}

	@Override
	public boolean isCompatibleWith(CType o) {
		if (o instanceof CPrimitive &&
				((CPrimitive) o).getType() == PRIMITIVE.VOID)
			return true;

		if (((Object) this).equals(o)) //to break a mutual recursion with CPointer -- TODO: is that a general solution??
    		return true;
        if (!(o instanceof CType)) {
            return false;
        }
        CType oType = ((CType)o).getUnderlyingType();
        if (!(oType instanceof CStruct)) {
            return false;
        }
        
        CStruct oStruct = (CStruct)oType;
        if (fNames.length != oStruct.fNames.length) {
            return false;
        }
//        for (int i = fNames.length - 1; i >= 0; --i) { //names of fields seem irrelevant for compatibility??
//            if (!(fNames[i].equals(oStruct.fNames[i]))) {
//                return false;
//            }
//        }
        if (fTypes.length != oStruct.fTypes.length) {
            return false;
        }
        for (int i = fTypes.length - 1; i >= 0; --i) {
            if (!(fTypes[i].equals(oStruct.fTypes[i]))) {
                return false;
            }
        }
        return true;    
	}
	
	
	@Override
	public int hashCode() {
		return HashUtils.hashJenkins(31, fNames, fTypes, incompleteName);
	}
}
