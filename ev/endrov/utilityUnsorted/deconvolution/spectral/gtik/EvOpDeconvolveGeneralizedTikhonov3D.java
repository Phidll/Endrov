/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.utilityUnsorted.deconvolution.spectral.gtik;

import cern.colt.matrix.tdouble.DoubleMatrix3D;
import endrov.typeImageset.EvStack;
import endrov.utilityUnsorted.deconvolution.DeconvPixelsStack;
import endrov.utilityUnsorted.deconvolution.Deconvolver3D;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralPaddingType;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralResizingType;

/**
 * Deconvolution in 3D using generalized tikhonov
 * @author Johan Henriksson
 *
 */
public class EvOpDeconvolveGeneralizedTikhonov3D extends Deconvolver3D
	{
	private final EvStack imPSF;
	private final DoubleMatrix3D stencil;
	private final SpectralResizingType resizing;
	private final double regParam;
	private final double threshold;
	private final SpectralPaddingType padding;
	
	 public EvOpDeconvolveGeneralizedTikhonov3D(EvStack imPSF, DoubleMatrix3D stencil, SpectralResizingType resizing,double regParam, double threshold, SpectralPaddingType padding) 
		 {
		 this.imPSF=imPSF;
		 this.stencil=stencil;
		 this.resizing=resizing;
		 this.regParam=regParam;
		 this.threshold=threshold;
		 this.padding=padding;
		 }
   
	protected DeconvPixelsStack internalDeconvolve(EvStack ipB)
		{
		if(padding.equals(SpectralPaddingType.PERIODIC))
			{
			DoublePeriodicGeneralizedTikhonov3D d=new DoublePeriodicGeneralizedTikhonov3D(imPSF, stencil, resizing, regParam, threshold);
			//public DoublePeriodicGeneralizedTikhonov3D(EvStack imPSF, DoubleMatrix3D stencil, SpectralResizingType resizing, double regParam, double threshold) {
			return d.internalDeconvolve(ipB);
			}
		else
			{
			DoubleReflexiveGeneralizedTikhonov3D d=new DoubleReflexiveGeneralizedTikhonov3D(imPSF, stencil, resizing, regParam, threshold);
			return d.internalDeconvolve(ipB);
			}
		}
	
	
	}
