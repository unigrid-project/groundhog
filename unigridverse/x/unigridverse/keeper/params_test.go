package keeper_test

import (
	"testing"

	"github.com/stretchr/testify/require"
	testkeeper "unigridverse/testutil/keeper"
	"unigridverse/x/unigridverse/types"
)

func TestGetParams(t *testing.T) {
	k, ctx := testkeeper.UnigridverseKeeper(t)
	params := types.DefaultParams()

	k.SetParams(ctx, params)

	require.EqualValues(t, params, k.GetParams(ctx))
}
