package overtone.sc;

import com.sun.jna.*;
import java.util.*;

public interface Types extends Library {

  // public static interface PrintString extends Callback {
  //   void Print(String data);
  // }

  // supercollider/include/plugin_interface/SC_Rate.h
  public static class Rate extends Structure {
    public double mSampleRate;
    public double mSampleDur;
    public double mBufDuration;
    public double mBufRate;
    public double mSlopeFactor;
    public double mRadiansPerSample;

    public int mBufLength;
    public int mFilterLoops;
    public int mFilterRemain;

    public double mFilterSlope;

    public static class ByReference extends Rate implements Structure.ByReference {}
    public static class ByValue extends Rate implements Structure.ByValue {}
    public Rate () {}
    public Rate (Pointer p ) { super(p); read(); }
    protected List getFieldOrder() {
      return Arrays.asList(new String[]
        { "mSampleRate", "mSampleDur", "mBufDuration", "mBufRate",
          "mSlopeFactor", "mRadiansPerSample", "mBufLength", "mFilterLoops",
          "mFilterRemain", "mFilterSlope" });
    }

  }

  // supercollider/include/plugin_interface/SC_World.h
  public static class World extends Structure {
    // a pointer to private implementation, not available to plug-ins.
    public Pointer hw;

     // a pointer to the table of function pointers that implement the plug-ins'
    // interface to the server.
    public Pointer ft;

    public double mSampleRate;

    public int mBufLength;
    public int mBufCounter;
    public int mNumAudioBusChannels;
    public int mNumControlBusChannels;
    public int mNumInputs;
    public int mNumOutputs;

    public float[] mAudioBus;
    public float[] mControlBus;

    public int[] mAudioBusTouched;
    public int[] mControlBusTouched;

    public int mNumSndBufs;
    public SndBuf mSndBufs;
    public SndBuf mSndBufsNonRealTimeMirror;
    public Pointer mSndBufUpdates;

    public Pointer mTopGroup;

    public Rate mFullRate;
    public Rate mBufRate;

    public int mNumRGens;

    public Pointer mRGen;

    public int mNumUnits;
    public int mNumGraphs;
    public int mNumGroups;
    public int mSampleOffset;

    public Pointer mNRTLock;

    public int mNumSharedControls;
    public float[] mSharedControls;

    public int mRealTime; // bool
    public int mRunning; // bool

    public int mDumpOSC;

    public Pointer mDriverLock;

    public float mSubsampleOffset;

    public int mVerbosity;
    public int mErrorNotification;
    public int mLocalErrorNotification;

    public int mRendezvous; // bool

    public String mRestrictedPath; // OSC commands to read/write data can only do it within this path, if specified

    public static class ByReference extends World implements Structure.ByReference {}
    public static class ByValue extends World implements Structure.ByValue {}
    public World () {}
    public World (Pointer p ) { super(p); read(); }
    protected List getFieldOrder() {
      return Arrays.asList(new String[]
        { "hw", "ft", "mSampleRate", "mBufLength", "mBufCounter", "mNumAudioBusChannels", "mNumControlBusChannels",
          "mNumInputs", "mNumOutputs", "mAudioBus", "mControlBus", "mAudioBusTouched", "mControlBusTouched",
          "mNumSndBufs", "mSndBufs", "mSndBufsNonRealTimeMirror", "mSndBufUpdates", "mTopGroup", "mFullRate",
          "mBufRate", "mNumRGens", "mRGen", "mNumUnits", "mNumGraphs", "mNumGroups", "mSampleOffset", "mNRTLock",
          "mNumSharedControls", "mSharedControls", "mRealTime", "mRunning", "mDumpOSC", "mDriverLock", "mSubsampleOffset",
          "mVerbosity", "mErrorNotification", "mLocalErrorNotification", "mRendezvous", "mRestrictedPath" });
    }
  }

  // supercollider/include/server/SC_WorldOptions.h
  public static class WorldOptions extends Structure {

    public String mPassword;
    public int mNumBuffers;
    public int mMaxLogins;
    public int mMaxNodes;
    public int mMaxGraphDefs;
    public int mMaxWireBufs;
    public int mNumAudioBusChannels;
    public int mNumInputBusChannels;
    public int mNumOutputBusChannels;
    public int mNumControlBusChannels;
    public int mBufLength;
    public int mRealTimeMemorySize;
    public int mNumSharedControls;
    public float mSharedControls;

    public int mRealTime; // bool
    public int mMemoryLocking; // bool

    public String mNonRealTimeCmdFilename;
    public String mNonRealTimeInputFilename;
    public String mNonRealTimeOutputFilename;
    public String mNonRealTimeOutputHeaderFormat;
    public String mNonRealTimeOutputSampleFormat;

    public int mPreferredSampleRate;
    public int mNumRGens;
    public int mPreferredHardwareBufferFrameSize;
    public int mLoadGraphDefs;

    public String mInputStreamsEnabled;
    public String mOutputStreamsEnabled;
    public String mInDeviceName;

    public int mVerbosity;

    public int mRendezvous; // bool

    public String mUGensPluginPath;
    public String mOutDeviceName;
    public String mRestrictedPath;

    public int mSharedMemoryID;

    public static class ByReference extends WorldOptions implements Structure.ByReference {}

    public static class ByValue extends WorldOptions implements Structure.ByValue {}

    public WorldOptions () {}

    public WorldOptions (Pointer p ) { super(p); read(); }

    protected List getFieldOrder() {
      return Arrays.asList(new String[]
        { "mPassword", "mNumBuffers", "mMaxLogins", "mMaxNodes", "mMaxGraphDefs",
          "mMaxWireBufs", "mNumAudioBusChannels", "mNumInputBusChannels",
          "mNumOutputBusChannels", "mNumControlBusChannels", "mBufLength",
          "mRealTimeMemorySize", "mNumSharedControls", "mSharedControls", "mRealTime",
          "mMemoryLocking", "mNonRealTimeCmdFilename", "mNonRealTimeInputFilename",
          "mNonRealTimeOutputFilename", "mNonRealTimeOutputHeaderFormat", "mNonRealTimeOutputSampleFormat",
          "mPreferredSampleRate", "mNumRGens", "mPreferredHardwareBufferFrameSize", "mLoadGraphDefs",
          "mInputStreamsEnabled", "mOutputStreamsEnabled", "mInDeviceName", "mVerbosity",
          "mRendezvous", "mUGensPluginPath", "mOutDeviceName", "mRestrictedPath", "mSharedMemoryID" });
    }
  }

  // supercollider/include/plugin_interface/SC_SndBuf.h
  public static class SndBuf extends Structure {
    public double samplerate;
    public double sampledur; // = 1/samplerate

    public float[] data;

    public int channels;
    public int samples;
    public int frames;
    public int mask; // for delay lines
    public int mask1; // for interpolating oscillators.
    public int coord; // used by fft ugens

    public Pointer sndfile; // used by disk i/o

    public static class ByReference extends SndBuf implements Structure.ByReference {}
    public static class ByValue extends SndBuf implements Structure.ByValue {}
    public SndBuf () {}
    public SndBuf (Pointer p ) { super(p); read(); }
    protected List getFieldOrder() {
      return Arrays.asList(new String[]
        { "samplerate", "sampledur", "data", "channels", "samples",
          "frames", "mask", "mask1", "coord", "sndfile" });
    }
  }

  // public static class SVMParameter extends Structure {
  //   public int svm_type;
  //   public int kernel_type;
  //   public int degree;	/* for poly */
  //   public double gamma;	/* for poly/rbf/sigmoid */
  //   public double coef0;	/* for poly/sigmoid */

  //   /* these are for training only */
  //   public double cache_size; /* in MB */
  //   public double eps;	/* stopping criteria */
  //   public double C;	/* for C_SVC, EPSILON_SVR and NU_SVR */
  //   public int nr_weight;		/* for C_SVC */
  //   public Pointer weight_label;	/* for C_SVC */
  //   public Pointer weight;		/* for C_SVC */
  //   public double nu;	/* for NU_SVC, ONE_CLASS, and NU_SVR */
  //   public double p;	/* for EPSILON_SVR */
  //   public int shrinking;	/* use the shrinking heuristics */
  //   public int probability; /* do probability estimates */

  //   public static class ByReference extends SVMParameter implements Structure.ByReference {}
  //   public static class ByValue extends SVMParameter implements Structure.ByValue {}
  //   public SVMParameter () {}
  //   public SVMParameter (Pointer p ) { super(p); read(); }
  //   protected List getFieldOrder() {
  //     return Arrays.asList(new String[]
  //       {
  //         "svm_type",
  //         "kernel_type",
  //         "degree",
  //         "gamma",
  //         "coef0",
  //         "cache_size",
  //         "eps",
  //         "C",
  //         "nr_weight",
  //         "weight_label",
  //         "weight",
  //         "nu",
  //         "p",
  //         "shrinking",
  //         "probability"
  //       });
  //   }
  // }

}
