import { POST_JOB, GET_JOB, GET_JOBS, SET_LOADING } from '../types';

export default (state, action) => {
  switch (action.type) {
    case POST_JOB:
      return {
        ...state,
        jobs: [action.payload, ...state.jobs],
        job: action.payload,
        loading: false,
      };
    default:
      return state;
  }
};
