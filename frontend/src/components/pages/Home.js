import React, { Fragment } from 'react';
import { Divider } from 'antd';

import JobSubmitForm from '../job/JobSubmitForm';
import JobList from '../job/JobList';

const Home = () => {
  return (
    <Fragment>
      <JobSubmitForm />
      <Divider />
      <JobList />
    </Fragment>
  );
};

export default Home;
