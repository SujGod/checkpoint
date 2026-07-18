import { configureStore } from "@reduxjs/toolkit";
import { checkpointApi } from "../api/checkpointApi";

export const store = configureStore({
  reducer: {
    [checkpointApi.reducerPath]: checkpointApi.reducer,
  },

  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(checkpointApi.middleware),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
