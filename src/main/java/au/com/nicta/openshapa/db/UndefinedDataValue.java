/*
 * UndefinedDataValue.java
 *
 * Created on August 19, 2007, 4:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package au.com.nicta.openshapa.db;

/**
 * An instance of UndefinedDataValue is used as a place holder for an untyped
 * formal argument until a value is assigned.
 *
 * @author mainzer
 */

public class UndefinedDataValue extends DataValue
{
    /*************************************************************************/
    /***************************** Fields: ***********************************/
    /*************************************************************************/
    /*
     * itsValue:   String containing the name of the formal argument.
     */
    
    /** the name of the associated formal arg */
    String itsValue = "<val>";
      
    
    /*************************************************************************/
    /*************************** Constructors: *******************************/
    /*************************************************************************/
    
    /** 
     * UndefinedDataValue()
     *
     * Constructor for instances of UndefinedDataValue.  
     * 
     * Three versions of this constructor.  
     * 
     * The first takes a reference to a database as its parameter and just 
     * calls the super() constructor.
     *
     * The second takes a reference to a database, a formal argument ID, and 
     * a value as arguments, and attempts to set the itsFargID and itsValue 
     * of the data value accordingly.
     *
     * The third takes a reference to an instance of UndefinedDataValue as an
     * argument, and uses it to create a copy.
     *
     *                                              JRM -- 8/16/07  
     *
     * Changes:
     *
     *    - None.
     *      
     */
 
    public UndefinedDataValue(Database db)
        throws SystemErrorException
    {
        super(db);
        
    } /* UndefinedDataValue::UndefinedDataValue(db) */
    
    public UndefinedDataValue(Database db,
                              long fargID,
                              String value)
        throws SystemErrorException
    {
        super(db);
        
        this.setItsFargID(fargID);
        
        this.setItsValue(value);
    
    } /* UndefinedDataValue::UndefinedDataValue(db, fargID, value) */
    
    public UndefinedDataValue(UndefinedDataValue dv)
        throws SystemErrorException
    {
        super(dv);
        
        this.itsValue  = new String(dv.itsValue);
        
    } /* UndefinedDataValue::UndefinedDataValue(dv) */
    
        
    /*************************************************************************/
    /***************************** Accessors: ********************************/
    /*************************************************************************/

    /**
     * getItsValue()
     *
     * Return a string containing a copy of the current value of the data value.
     *
     *                          JRM -- 8/16/07
     *
     * Changes:
     *
     *    - None.
     */
    
    public String getItsValue()
    {
        
        return new String(this.itsValue);
    
    } /* UndefinedDataValue::getItsValue() */
    
    /**
     * setItsValue()
     *
     * Set itsValue to the specified value.  In the case of an undefined 
     * data value, the value must be the name of the associated untyped 
     * formal argument, or any valid formal argument name if itsFargID
     * is undefined.
     *
     *                                              JRM -- 8/16/07
     *
     * Changes:
     *
     *    - With the advent of column predicates and the prospect of 
     *      implementing the old MacSHAPA query language in OpenSHAPA,
     *      the requirement that undefined data values only be used to 
     *      replace untyped formal arguments is removed.
     *
     *                                              JRM -- 12/12/08
     */
    
    public void setItsValue(String value)
        throws SystemErrorException
    {
        final String mName = "UndefinedDataValue::setItsValue(): ";
        
        if ( ! ( db.IsValidFargName(value) ) )
        {
            throw new SystemErrorException(mName +
                    "value isn't a valid formal argument name");
        }
        
        if ( this.itsFargID != DBIndex.INVALID_ID )
        {
            DBElement dbe;
            FormalArgument fa;
            
            if ( itsFargType == FormalArgument.fArgType.UNDEFINED )
            {
                throw new SystemErrorException(mName + 
                                               "itsFargType == UNDEFINED");
            }
            
            dbe = this.db.idx.getElement(this.itsFargID);

            if ( dbe == null )
            {
                throw new SystemErrorException(mName + 
                                               "itsFargID has no referent");
            }
            
            if ( ! ( dbe instanceof FormalArgument ) )
            {
                throw new SystemErrorException(mName +
                        "itsFargID doesn't refer to a formal arg");
            }
            
            fa = (FormalArgument)dbe;
            
            if ( fa.getFargName().compareTo(value) != 0 )
            {
                throw new SystemErrorException(mName + 
                        "value doesn't match farg name");
            }
        }
        
        this.itsValue = new String(value);
        
        return;
        
    } /* UndefinedDataValue::setItsValue() */
  
        
    /*************************************************************************/
    /*************************** Overrides: **********************************/
    /*************************************************************************/
    
    /**
     * toString()
     *
     * Returns a String representation of the DBValue for display.
     *
     *                                  JRM -- 8/15/07
     *
     * @return the string value.
     *
     * Changes:
     *
     *     - None.
     */
    
    public String toString()
    {
        return new String(this.itsValue);
    }


    /**
     * toDBString()
     *
     * Returns a database String representation of the DBValue for comparison 
     * against the database's expected value.<br>
     * <i>This function is intended for debugging purposses.</i>
     *
     *                                      JRM -- 8/15/07
     *
     * @return the string value.
     *
     * Changes:
     *
     *    - None.
     */
  
    public String toDBString()
    {
        return ("(UndefinedDataValue (id " + this.id +
                ") (itsFargID " + this.itsFargID +
                ") (itsFargType " + this.itsFargType +
                ") (itsCellID " + this.itsCellID +
                ") (itsValue " + new String(this.itsValue) +
                ") (subRange " + this.subRange + "))");
    }
    
    
    /** 
     * updateForFargChange()
     *
     * Update for a change in the formal argument name, and/or subrange.
     *
     *                                          JRM -- 3/22/08
     *
     * Changes:
     *
     *    - None.
     */
    
    public void updateForFargChange(boolean fargNameChanged,
                                    boolean fargSubRangeChanged,
                                    boolean fargRangeChanged,
                                    FormalArgument oldFA,
                                    FormalArgument newFA)
        throws SystemErrorException
    {
        final String mName = "TimeStampDataValue::updateForFargChange(): ";
        
        if ( ( oldFA == null ) || ( newFA == null ) )
        {
            throw new SystemErrorException(mName + 
                                           "null old and/or new FA on entry.");
        }
        
        if ( oldFA.getID() != newFA.getID() )
        {
            throw new SystemErrorException(mName + "old/new FA ID mismatch.");
        }
        
        if ( oldFA.getItsVocabElementID() != newFA.getItsVocabElementID() )
        {
            throw new SystemErrorException(mName + "old/new FA veID mismatch.");
        }
        
        if ( oldFA.getFargType() != newFA.getFargType() )
        {
            throw new SystemErrorException(mName + "old/new FA type mismatch.");
        }
        
        if ( this.itsFargID != newFA.getID() )
        {
            throw new SystemErrorException(mName + "FA/DV faID mismatch.");
        }
        
        if ( this.itsFargType != newFA.getFargType() )
        {
            throw new SystemErrorException(mName + "FA/DV FA type mismatch.");
        }
         
        if ( ( fargSubRangeChanged ) || ( fargRangeChanged ) ) 
        {
            this.updateSubRange(newFA);
        }
        
        if ( fargNameChanged )
        {
            this.setItsValue(newFA.getFargName());
        }
        
        return;
        
    } /* TimeStampDataValue::updateForFargChange() */
    
    
    /**
     * updateSubRange()
     *
     * Nominally, this function should determine if the formal argument 
     * associated with the data value is subranged, and if it is, update
     * the data values representation of the subrange (if any) accordingly.  
     * In passing, it would coerce the value ofthe datavalue into the subrange 
     * if necessary.
     *
     * This is meaningless for an undefine data value, as it never has a 
     * value, and it is only associated with untyped formal arguments.
     *
     * Thus the method verifies that the supplied formal argument is an
     * UnTypedFormalArg, and that the value of the data value equals the
     * name of the formal argument.
     *
     * The fa argument is a reference to the current representation of the
     * formal argument associated with the data value.
     *
     *                                          JRM -- 8/16/07
     *
     * Changes:
     *
     *    - None.
     */
    
    protected void updateSubRange(FormalArgument fa)
        throws SystemErrorException
    {
        final String mName = "UndefinedDataValue::updateSubRange(): ";
        UnTypedFormalArg utfa;
        
        if ( fa == null )
        {
            throw new SystemErrorException(mName + "fa null on entry");    
        }
        
        if ( fa instanceof UnTypedFormalArg )
        {
             this.subRange = false;
        }
        else
        {
            throw new SystemErrorException(mName + "Unexpected fa type");    
        }
        
        utfa = (UnTypedFormalArg)fa;
        
        if ( utfa.getFargName().compareTo(this.itsValue) != 0 )
        {
            throw new SystemErrorException(mName + "farg name mismatch");    
        }
        
        return;
        
    } /* UndefinedDataValue::updateSubRange() */
  
        
    /*************************************************************************/
    /***************************** Methods: **********************************/
    /*************************************************************************/
    
    /**
     * coerceToRange()
     *
     * The value of an UndefinedDataValue must be a valid formal argument name.
     * In addition, if the data value is associated with a formal argument
     * (always an UnTypedFormalArgument), its value must be the name of the
     * formal argument.
     *
     * Thus, coerce to the name of the associated UnTypedFormalArg if defined.
     *
     * Throw a system error if the value is not a valid formal argument name.
     * 
     *                                              JRM -- 070815
     *
     * Changes:
     *
     *    - None.
     */
    
    public String coerceToRange(String value)
        throws SystemErrorException
    {
        final String mName = "UndefinedDataValue::coerceToRange(): ";

        if ( ! ( db.IsValidFargName(value) ) )
        {
            throw new SystemErrorException(mName + 
                    "value not a valid formal argument name");
        }
        
        if ( this.itsFargID != DBIndex.INVALID_ID )
        {
            DBElement dbe;
            UnTypedFormalArg utfa;
            
            if ( itsFargType != FormalArgument.fArgType.UNTYPED )
            {
                throw new SystemErrorException(mName + 
                                               "itsFargType != UNTYPED");
            }
            
            dbe = this.db.idx.getElement(this.itsFargID);

            if ( dbe == null )
            {
                throw new SystemErrorException(mName + 
                                               "itsFargID has no referent");
            }
            
            if ( ! ( dbe instanceof UnTypedFormalArg ) )
            {
                throw new SystemErrorException(mName +
                        "itsFargID doesn't refer to an untyped formal arg");
            }
            
            utfa = (UnTypedFormalArg)dbe;
            
            if ( utfa.getFargName().compareTo(value) != 0 )
            {
                return new String(utfa.getFargName());
            }
        }
        
        return value;
        
    } /* UndefinedDataValue::coerceToRange() */
  
    
    /*************************************************************************/
    /************************ Class Methods: *********************************/
    /*************************************************************************/
    
    /**
     * Construct()
     *
     * Construct an instance of UndefinedDataValue with the specified 
     * initialization.
     *
     * Returns a reference to the newly constructed UndefinedDataValue if 
     * successful.  Throws a system error exception on failure.
     *
     *                                              JRM -- 3/31/08
     *
     * Changes:
     *
     *    - None.
     */
    
    public static UndefinedDataValue Construct(Database db)
        throws SystemErrorException
    {
        final String mName = "UndefinedDataValue::Construct(db)";
        UndefinedDataValue udv = null;
        
        udv = new UndefinedDataValue(db);
        
        return udv;
        
    } /* UndefinedDataValue::Construct(db) */

      
    /**
     * UndefinedDataValuesAreLogicallyEqual()
     *
     * Given two instances of UndefinedDataValue, return true if they contain 
     * identical data, and false otherwise.  Strange as it may seem, it is 
     * possible for two undefined data values to have different values, as
     * the value of an UndefinedDataValue is simply the name of the associated
     * formal argument -- which can change.
     *
     * Note that this method does only tests specific to this subclass of 
     * DataValue -- the presumption is that this method has been called by 
     * DataValue.DataValuesAreLogicallyEqual() which has already done all
     * generic tests.
     * 
     *                                              JRM -- 2/7/08
     *
     * Changes:
     *
     *    - None.
     */
    
    protected static boolean UndefinedDataValuesAreLogicallyEqual
            (UndefinedDataValue udv0,
             UndefinedDataValue udv1)
        throws SystemErrorException
    {
        final String mName = 
            "UndefinedDataValue::UndefinedDataValuesAreLogicallyEqual()";
        boolean dataValuesAreEqual = true;
        
        if ( ( udv0 == null ) || ( udv1 == null ) )
        {
            throw new SystemErrorException(mName + 
                                           ": udv0 or udv1 null on entry.");
        }
        else if ( ( udv0.itsValue == null ) || ( udv1.itsValue == null ) )
        {
            throw new SystemErrorException(mName + 
                    ": udv0.itsValue or udv1.itsValue null on entry.");
        }
        
        if ( udv0 != udv1 )
        {
            if ( udv0.itsValue != udv1.itsValue )
            {
                // due to above tests, if we get this far, we know
                // that both udv0.itsValue and udv1.itsValue are non-null.
                if ( udv0.itsValue.compareTo(udv1.itsValue) != 0 )
                {
                    dataValuesAreEqual = false;
                }
            }
        }

        return dataValuesAreEqual;
        
    } /* UndefinedDataValue::UndefinedDataValuesAreLogicallyEqual() */

    
    /*************************************************************************/
    /**************************** Test Code: *********************************/
    /*************************************************************************/

    // TODO: Write test suite for undefined data values.
    
    /**
     * VerifyUndefinedDVCopy()
     *
     * Verify that the supplied instances of UndefinedDataValue are distinct, 
     * that they contain no common references (other than db), and that they 
     * have the same value.
     *                                              JRM -- 11/8/07
     *
     * Changes:
     *
     *    - None
     */

    public static int VerifyUndefinedDVCopy(UndefinedDataValue base,
                                            UndefinedDataValue copy,
                                            java.io.PrintStream outStream,
                                            boolean verbose,
                                            String baseDesc,
                                            String copyDesc)
    {
        int failures = 0;

        if ( base == null )
        {
            failures++;
            outStream.printf("VerifyUndefinedDVCopy: %s null on entry.\n", 
                             baseDesc);
        }
        else if ( copy == null )
        {
            failures++;
            outStream.printf("VerifyUndefinedDVCopy: %s null on entry.\n", 
                             copyDesc);
        }
        else if ( base == copy )
        {
            failures++;

            if ( verbose )
            {
                outStream.printf("%s == %s.\n", baseDesc, copyDesc);
            }
        }
        else if ( base.db != copy.db )
        {
            failures++;

            if ( verbose )
            {
                outStream.printf("%s.db != %s.db.\n", baseDesc, copyDesc);
            }
        }
        else if ( ( base.itsValue == copy.itsValue ) &&
                  ( base.itsValue != null ) )
        {
            failures++;

            if ( verbose )
            {
                outStream.printf("%s and %s share a string.\n", 
                                  baseDesc, copyDesc);
            }
        }
        else if ( ( base.itsValue == copy.itsValue ) &&
                  ( base.itsValue != null ) )
        {
            failures++;

            if ( verbose )
            {
                outStream.printf(
                        "%s.itsValue is null, and %s.itsValue isn't.\n",
                        baseDesc, copyDesc);
            }
        }
        else if ( ( base.itsValue != copy.itsValue ) &&
                  ( copy.itsValue == null ) )
        {
            failures++;

            if ( verbose )
            {
                outStream.printf(
                        "%s.itsValue is null, and %s.itsValue isn't.\n",
                        copyDesc, baseDesc);
            }
        }
        else if ( ( base.itsValue != copy.itsValue ) &&
                  ( base.itsValue.compareTo(copy.itsValue) != 0 ) )
        {
            failures++;

            if ( verbose )
            {
                outStream.printf("%s and %s contain different values.\n", 
                                  baseDesc, copyDesc);
            }
        }
        else if ( base.toString().compareTo(copy.toString()) != 0 )
        {
            failures++;

            if ( verbose )
            {
                outStream.printf("%s.toString() doesn't match %s.toString().\n", 
                                 baseDesc, copyDesc);
            }
        }
        else if ( base.toDBString().compareTo(copy.toDBString()) != 0 )
        {
            failures++;

            if ( verbose )
            {
                outStream.printf(
                        "%s.toDBString() doesn't match %s.toDBString().\n", 
                        baseDesc, copyDesc);
            }
        }

        return failures;

    } /* UndefinedDataValue::VerifyUndefinedDVCopy() */

} /* UndefinedDataValue */
