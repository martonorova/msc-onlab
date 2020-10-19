import React, { useReducer } from 'react';
import axios from 'axios';
import JobContext from './jobContext';
import JobReducer from './jobReducer';
import { POST_JOB, GET_JOB, GET_JOBS, SET_LOADING } from '../types';

import { API_URL } from '../../constants/apiConstants';

const JobState = (props) => {
  const initialState = {
    jobs: [],
    job: {},
    loading: false,
  };

  const [state, dispatch] = useReducer(JobReducer, initialState);

  // Get Jobs
  const getJobs = async () => {
    setLoading();
  };

  // Get Job
  const getJob = async (id) => {
    setLoading();
  };

  // Post Jobs
  const postJob = async (job) => {
    setLoading();

    const config = {
      headers: {
        'Content-Type': 'application/json',
      },
    };

    try {
      const res = await axios.post(`${API_URL}/jobs`, job, config);

      dispatch({
        type: POST_JOB,
        payload: res.data,
      });
    } catch (err) {
      console.error(err);
    }
  };

  // Set loading
  const setLoading = () => dispatch({ type: SET_LOADING });

  return (
    <JobContext.Provider
      value={{
        jobs: state.jobs,
        job: state.job,
        loading: state.loading,
        getJob,
        getJobs,
        postJob,
      }}
    >
      {props.children}
    </JobContext.Provider>
  );
};

export default JobState;
