let navigateFunction = null;

export const setNavigate = (navigate) => {
  navigateFunction = navigate;
};

export const navigate = (path) => {
  if (navigateFunction) {
    navigateFunction(path);
  } else {
    console.error("navigate function is not present");
  }
};
