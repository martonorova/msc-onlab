import { POST_JOB, GET_JOB, GET_JOBS, SET_LOADING } from '../types';

export default (state, action) => {
  switch (action.type) {
    case GET_JOBS:
      return {
        ...state,
        jobs: action.payload,
        loading: false,
      };
    case POST_JOB:
      return {
        ...state,
        jobs: [...state.jobs, action.payload],
        job: action.payload,
        loading: false,
      };
    case SET_LOADING:
      return {
        ...state,
        loading: true,
      };
    default:
      return state;
  }
};
