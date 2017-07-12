package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.Result;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.actions.TaskInfoAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.GenericStatus;
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
public class NodeRunStatus {
    public final BlueRun.BlueRunResult result;
    public final BlueRun.BlueRunState state;

    public NodeRunStatus(@Nonnull FlowNode endNode) {
        Result result = null;
        ErrorAction errorAction = endNode.getError();
        if (errorAction != null) {
            if(errorAction.getError() instanceof FlowInterruptedException) {
                result = ((FlowInterruptedException) errorAction.getError()).getResult();
            }
            if(result == null || result != Result.ABORTED) {
                this.result = BlueRun.BlueRunResult.FAILURE;
            } else {
                this.result = BlueRun.BlueRunResult.ABORTED;
            }
            this.state = endNode.isRunning() ? BlueRun.BlueRunState.RUNNING : BlueRun.BlueRunState.FINISHED;
        } else if (TaskInfoAction.getNodeState(endNode) == TaskInfoAction.QueueState.QUEUED) {
            this.result = BlueRun.BlueRunResult.UNKNOWN;
            this.state = BlueRun.BlueRunState.QUEUED;
        } else if (TaskInfoAction.getNodeState(endNode) == TaskInfoAction.QueueState.CANCELLED) {
            this.result = BlueRun.BlueRunResult.ABORTED;
            this.state = BlueRun.BlueRunState.FINISHED;
        } else if (endNode.isRunning()) {
            this.result = BlueRun.BlueRunResult.UNKNOWN;
            this.state = BlueRun.BlueRunState.RUNNING;
        } else if (NotExecutedNodeAction.isExecuted(endNode)) {
            this.result = BlueRun.BlueRunResult.SUCCESS;
            this.state = BlueRun.BlueRunState.FINISHED;
        } else {
            this.result = BlueRun.BlueRunResult.NOT_BUILT;
            this.state = BlueRun.BlueRunState.QUEUED;
        }
    }

    public NodeRunStatus(BlueRun.BlueRunResult result, BlueRun.BlueRunState state) {
        this.result = result;
        this.state = state;
    }

    public BlueRun.BlueRunResult getResult() {
        return result;
    }

    public BlueRun.BlueRunState getState() {
        return state;
    }

    public NodeRunStatus(GenericStatus status){
        if (status == null) {
            this.result = BlueRun.BlueRunResult.NOT_BUILT;
            this.state = BlueRun.BlueRunState.QUEUED;
            return;
        }
        switch (status) {
            case PAUSED_PENDING_INPUT:
                this.result =  BlueRun.BlueRunResult.UNKNOWN;
                this.state =  BlueRun.BlueRunState.PAUSED;
                break;
            case ABORTED:
                this.result =  BlueRun.BlueRunResult.ABORTED;
                this.state =  BlueRun.BlueRunState.FINISHED;
                break;
            case FAILURE:
                this.result =  BlueRun.BlueRunResult.FAILURE;
                this.state =  BlueRun.BlueRunState.FINISHED;
                break;
            case IN_PROGRESS:
                this.result =  BlueRun.BlueRunResult.UNKNOWN;
                this.state =  BlueRun.BlueRunState.RUNNING;
                break;
            case UNSTABLE:
                this.result =  BlueRun.BlueRunResult.UNSTABLE;
                this.state =  BlueRun.BlueRunState.FINISHED;
                break;
            case SUCCESS:
                this.result =  BlueRun.BlueRunResult.SUCCESS;
                this.state =  BlueRun.BlueRunState.FINISHED;
                break;
            case NOT_EXECUTED:
                this.result = BlueRun.BlueRunResult.NOT_BUILT;
                this.state = BlueRun.BlueRunState.NOT_BUILT;
                break;
            case QUEUED:
                this.result = BlueRun.BlueRunResult.UNKNOWN;
                this.state = BlueRun.BlueRunState.QUEUED;
                break;
            default:
                // Shouldn't happen, above includes all statuses
                this.result = BlueRun.BlueRunResult.NOT_BUILT;
                this.state = BlueRun.BlueRunState.QUEUED;
        }
    }
}
