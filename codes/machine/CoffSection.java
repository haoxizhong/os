// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import java.io.EOFException;
import java.util.Arrays;

/**
 * A <tt>CoffSection</tt> manages a single section within a COFF executable.
 */
public class CoffSection {
    /**
     * Load a COFF section from an executable.
     *
     * @param	file		the file containing the executable.
     * @param	headerOffset	the offset of the section header in the
     * 				executable.
     *
     * @exception	EOFException	if an error occurs.
     */
    public CoffSection(OpenFile file, int headerOffset) throws EOFException {
	this.file = file;

	Lib.assert(headerOffset >= 0);
	if (headerOffset+headerLength > file.length()) {
	    Lib.debug(dbgCoffSection, "\tsection header truncated");
	    throw new EOFException();
	}
	
	byte[] buf = new byte[headerLength];
	Lib.strictReadFile(file, headerOffset, buf, 0, headerLength);
	
	name = Lib.bytesToString(buf, 0, 8);
	vaddr = Lib.bytesToInt(buf, 12);
	size = Lib.bytesToInt(buf, 16);
	contentOffset = Lib.bytesToInt(buf, 20);
	numRelocations = Lib.bytesToUnsignedShort(buf, 32);
	flags = Lib.bytesToInt(buf, 36);

	if (vaddr%Processor.pageSize != 0 || size < 0 || 
	    contentOffset < 0 || contentOffset+size > file.length()) {
	    Lib.debug(dbgCoffSection, "\tinvalid section addresses: " +
		      "vaddr=" + vaddr + " size=" + size +
		      " contentOffset=" + contentOffset);
	    throw new EOFException();
	}

	if (numRelocations != 0) {
	    Lib.debug(dbgCoffSection, "\tsection needs relocation");
	    throw new EOFException();
	}

	switch (flags & 0x0FFF) {
	case 0x0020:
	    executable = true;
	    readOnly = true;
	    initialized = true;
	    break;
	case 0x0040:
	    executable = false;
	    readOnly = false;
	    initialized = true;
	    break;
	case 0x0080:
	    executable = false;
	    readOnly = false;
	    initialized = false;
	    break;
	case 0x0100:
	    executable = false;
	    readOnly = true;
	    initialized = true;
	    break;
	default:
	    Lib.debug(dbgCoffSection, "\tinvalid section flags: " + flags);
	    throw new EOFException();
	}

	numPages = Lib.divRoundUp(size, Processor.pageSize);
	firstVPN = vaddr / Processor.pageSize;
    }

    /**
     * Return the name of this section.
     *
     * @return	the name of this section.
     */
    public String getName() {
	Lib.assert(file != null);
	
	return name;
    }

    /**
     * Test whether this section is read-only.
     *
     * @return	<tt>true</tt> if this section should never be written.
     */
    public boolean isReadOnly() {
	Lib.assert(file != null);
	
	return readOnly;
    }

    /**
     * Test whether this section is initialized. Loading a page from an
     * initialized section requires a disk access, while loading a page from an
     * uninitialized section requires only zero-filling the page.
     *
     * @return	<tt>true</tt> if this section contains initialized data in the
     *		executable.
     */
    public boolean isInitialzed() {
	Lib.assert(file != null);
	
	return initialized;
    }

    /**
     * Return the length of this section in pages.
     *
     * @return	the number of pages in this section.
     */
    public int getLength() {
	Lib.assert(file != null);
	
	return numPages;
    }

    /**
     * Return the first virtual page number used by this section.
     *
     * @return	the first virtual page number used by this section.
     */
    public int getFirstVPN() {
	Lib.assert(file != null);
	
	return firstVPN;
    }

    /**
     * Load a page from this segment into physical memory.
     *
     * @param	spn	the page number within this segment.
     * @param	ppn	the physical page to load into.
     * @return	<tt>true</tt> if successful.
     */
    public void loadPage(int spn, int ppn) {
	Lib.assert(file != null);
	
	Lib.assert(spn>=0 && spn<numPages);
	Lib.assert(ppn>=0 && ppn<Machine.processor().getNumPhysPages());
	    
	int pageSize = Processor.pageSize;
	byte[] memory = Machine.processor().getMemory();
	int paddr = ppn*pageSize;
	int faddr = contentOffset + spn*pageSize;
	int initlen;

	if (!initialized)
	    initlen = 0;
	else if (spn == numPages-1)
	    initlen = size % pageSize;
	else
	    initlen = pageSize;

	Lib.strictReadFile(file, faddr, memory, paddr, initlen);

	Arrays.fill(memory, paddr+initlen, paddr+pageSize, (byte) 0);
    }

    private OpenFile file;

    private String name;
    private int vaddr, size, contentOffset, flags;
    private int numRelocations;

    private boolean executable, readOnly, initialized;
    private int numPages, firstVPN;

    /** The length of a COFF section header. */
    public static final int headerLength = 40;

    private static final char dbgCoffSection = 'c';
}
