import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

import type { FactCheckRequest, FactCheckResponse } from "../types/fact-check";

export const claimsiftApi = createApi({
  reducerPath: "claimsiftApi",

  baseQuery: fetchBaseQuery({
    baseUrl: "http://localhost:8080/api/v1",
  }),

  endpoints: (builder) => ({
    checkClaim: builder.mutation<FactCheckResponse, FactCheckRequest>({
      query: (request: FactCheckRequest) => ({
        url: "/checks",
        method: "POST",
        body: request,
      }),
    }),
  }),
});

export const { useCheckClaimMutation } = claimsiftApi;
