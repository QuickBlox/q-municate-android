package com.quickblox.q_municate.service;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Trigger;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.q_municate.utils.StringObfuscator;
import com.quickblox.q_municate.utils.helpers.FirebaseAuthHelper;
import com.quickblox.q_municate.utils.helpers.ServiceManager;
import com.quickblox.users.model.QBUser;

import rx.Observer;

public class SessionJobService extends JobService {

    public static final String SIGN_IN_SOCIAL_ACTION = "sign_in_social";

    private static final String TAG = SessionJobService.class.getSimpleName();

    public static void startSignInSocial(Context context, Bundle bundle){
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        Job myJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(SessionJobService.class)
                // uniquely identifies the job
                .setTag(SIGN_IN_SOCIAL_ACTION)
                // one-off job
                .setRecurring(false)
                // start between 0 and 60 seconds from now
                .setTrigger(Trigger.executionWindow(0, 60))
                // don't overwrite an existing job with the same tag
                .setReplaceCurrent(false)
                // constraints that need to be satisfied for the job to run
                .setConstraints(
                        // only run on an unmetered network
                        Constraint.ON_UNMETERED_NETWORK
                )
                .setExtras(bundle)
                .build();

        dispatcher.mustSchedule(myJob);
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        // Do some work here
        Log.i(TAG, "onStartJob " + jobParameters.getTag());

        if (SIGN_IN_SOCIAL_ACTION.equals(jobParameters.getTag())) {
            Bundle jobExtras = jobParameters.getExtras();
            ServiceManager.getInstance().login(QBProvider.FIREBASE_PHONE,
                    jobExtras.getString(FirebaseAuthHelper.EXTRA_FIREBASE_ACCESS_TOKEN),
                    StringObfuscator.getFirebaseAuthProjectId())
                    .subscribe( new JobObserver(this, jobParameters));
            return true;
        }

        return false; // Answers the question: "Is there still work going on?"*/
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.i(TAG, "onStopJob " + job.getTag());
        return false;// Answers the question: "Should this job be retried?"
    }

    private static class JobObserver implements Observer<QBUser> {

        private SessionJobService jobService;
        private JobParameters parameters;

        JobObserver(SessionJobService jobService, JobParameters parameters){
            this.jobService = jobService;
            this.parameters = parameters;
        }

        @Override
        public void onCompleted() {
            Log.i(TAG, "onCompleted ");
        }

        @Override
        public void onError(Throwable e) {
            Log.i(TAG, "onError " + e.getLocalizedMessage());
            jobService.jobFinished(parameters, true);
        }

        @Override
        public void onNext(QBUser qbUser) {
            Log.i(TAG, "onNext " + qbUser.getLogin());
            jobService.jobFinished(parameters, false);
        }
    }
}